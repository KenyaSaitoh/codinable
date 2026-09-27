// 演習で使うテーブルの説明 (SCHEMA.md の本文) を、実際の DDL / DML から作る
//
// 雛形の SQL を HSQLDB (インメモリ) に流し、できたテーブルの構成を JDBC のメタデータから読む
// 手で書くと DDL と食い違っていくので、説明は必ずこれで作り直す
// 各カラムの意味は DDL の行末コメント (EMPLOYEE_ID INT, -- 社員ID) から、
// テーブルの意味は CREATE TABLE の直前のコメント行から取る
//
//   java -cp <hsqldb.jar> SchemaDoc.java --sql  <file.sql> ...   … SQL ファイルを流す
//   java -cp <hsqldb.jar> SchemaDoc.java --java <File.java> ...  … Java の文字列に書かれた
//                                                                   CREATE TABLE / INSERT を流す
//
// 出力は Markdown (テーブルごとの見出し・カラムの表・初期データ)。前置きは呼び出し側
// (scripts/build-schema-docs.js) が付ける
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

public class SchemaDoc {
    static final int MAX_ROWS = 12;

    public static void main(String[] args) throws Exception {
        String mode = args[0];
        List<String> sources = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            sources.add(Files.readString(Path.of(args[i]), StandardCharsets.UTF_8));
        }

        List<String> statements = new ArrayList<>();
        StringBuilder ddlText = new StringBuilder();
        for (String src : sources) {
            if (mode.equals("--java")) {
                for (String sql : sqlInJava(src)) { statements.add(sql); ddlText.append(sql).append(";\n"); }
            } else {
                statements.addAll(split(src));
                ddlText.append(src).append('\n');
            }
        }

        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        try (Connection con = DriverManager.getConnection("jdbc:hsqldb:mem:schemadoc;sql.syntax_mys=false", "SA", "")) {
            try (Statement st = con.createStatement()) {
                for (String sql : statements) {
                    try { st.execute(sql); }
                    catch (SQLException e) {
                        // 最初の DROP は「無ければ失敗」でよい。それ以外は知らせる
                        if (!sql.trim().toUpperCase().startsWith("DROP")) {
                            System.err.println("[SchemaDoc] failed: " + e.getMessage() + "\n  " + sql);
                        }
                    }
                }
            }
            System.out.print(render(con, ddlText.toString()));
        }
    }

    // ── SQL の取り出し ─────────────────────────────────────

    /** 行コメントを除いて ; で分ける (文字列リテラル中の -- と ; は残す) */
    static List<String> split(String src) {
        StringBuilder clean = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            if (c == '\'') inQuote = !inQuote;
            if (!inQuote && c == '-' && i + 1 < src.length() && src.charAt(i + 1) == '-') {
                while (i < src.length() && src.charAt(i) != '\n') i++;
                clean.append('\n');
                continue;
            }
            clean.append(c);
        }
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        inQuote = false;
        for (char c : clean.toString().toCharArray()) {
            if (c == '\'') inQuote = !inQuote;
            if (c == ';' && !inQuote) { if (!cur.toString().isBlank()) out.add(cur.toString().trim()); cur.setLength(0); }
            else cur.append(c);
        }
        if (!cur.toString().isBlank()) out.add(cur.toString().trim());
        return out;
    }

    /** Java の execute(...) / executeUpdate(...) などに渡している文字列をつなげて取り出す */
    static List<String> sqlInJava(String src) {
        List<String> out = new ArrayList<>();
        Matcher call = Pattern.compile("execute(?:Update)?\\s*\\(((?:\"(?:[^\"\\\\]|\\\\.)*\"|[^;])*?)\\)\\s*;", Pattern.DOTALL).matcher(src);
        Pattern lit = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"");
        while (call.find()) {
            StringBuilder sql = new StringBuilder();
            Matcher m = lit.matcher(call.group(1));
            while (m.find()) sql.append(m.group(1).replace("\\n", "\n").replace("\\'", "'").replace("\\\"", "\""));
            String s = sql.toString().trim();
            String head = s.toUpperCase();
            if (head.startsWith("CREATE TABLE") || head.startsWith("INSERT") || head.startsWith("ALTER TABLE")) out.add(s);
        }
        return out;
    }

    // ── DDL のコメント ────────────────────────────────────

    /** テーブル名 → { "": テーブルの説明, カラム名: カラムの説明 } */
    static Map<String, Map<String, String>> comments(String ddl) {
        Map<String, Map<String, String>> result = new HashMap<>();
        String[] lines = ddl.split("\\R");
        Pattern create = Pattern.compile("(?i)^\\s*CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?\"?(\\w+)\"?");
        Pattern column = Pattern.compile("^\\s*\"?(\\w+)\"?\\s+[^-]*?--\\s*(.+?)\\s*$");
        String table = null;
        for (int i = 0; i < lines.length; i++) {
            Matcher c = create.matcher(lines[i]);
            if (c.find()) {
                table = c.group(1).toUpperCase();
                Map<String, String> map = result.computeIfAbsent(table, k -> new HashMap<>());
                // 直前のコメント行 (空行をはさまない) がテーブルの名前 (「社員テーブル」など) なら
                // 見出しに添える。文になっている注記 (「…ため、…しない」) は名前ではないので使わない
                for (int j = i - 1; j >= 0 && lines[j].trim().startsWith("--"); j--) {
                    String text = lines[j].trim().replaceFirst("^-+\\s*", "").replaceAll("[─━]+", "").trim();
                    if (text.isEmpty()) continue;
                    if (text.length() <= 20 && !text.matches(".*[、。=＝()（）].*")) map.put("", text);
                    break;
                }
                continue;
            }
            if (table != null) {
                if (lines[i].trim().startsWith(");") || lines[i].trim().equals(")")) { table = null; continue; }
                Matcher m = column.matcher(lines[i]);
                if (m.find()) result.get(table).put(m.group(1).toUpperCase(), m.group(2));
            }
        }
        return result;
    }

    // ── Markdown ─────────────────────────────────────────

    static String render(Connection con, String ddl) throws SQLException {
        DatabaseMetaData md = con.getMetaData();
        Map<String, Map<String, String>> notes = comments(ddl);

        // DDL に書かれた順に並べる (メタデータの順は名前順で、読む順番にならない)
        List<String> tables = new ArrayList<>();
        Matcher c = Pattern.compile("(?i)CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?\"?(\\w+)\"?").matcher(ddl);
        while (c.find()) { String t = c.group(1).toUpperCase(); if (!tables.contains(t)) tables.add(t); }
        tables.removeIf(t -> !exists(md, t));

        StringBuilder out = new StringBuilder();
        for (String table : tables) {
            Map<String, String> note = notes.getOrDefault(table, Map.of());
            String title = note.containsKey("") ? table + "（" + note.get("").replaceAll("テーブル$", "") + "）" : table;
            out.append("## ").append(title).append("\n\n");

            Set<String> pk = new LinkedHashSet<>();
            try (ResultSet rs = md.getPrimaryKeys(null, "PUBLIC", table)) { while (rs.next()) pk.add(rs.getString("COLUMN_NAME")); }
            Map<String, String> fk = new HashMap<>();
            try (ResultSet rs = md.getImportedKeys(null, "PUBLIC", table)) {
                while (rs.next()) fk.put(rs.getString("FKCOLUMN_NAME"), rs.getString("PKTABLE_NAME") + "." + rs.getString("PKCOLUMN_NAME"));
            }
            Set<String> unique = new HashSet<>();
            try (ResultSet rs = md.getIndexInfo(null, "PUBLIC", table, true, false)) {
                Map<String, List<String>> byIndex = new HashMap<>();
                while (rs.next()) {
                    if (rs.getString("COLUMN_NAME") == null) continue;
                    byIndex.computeIfAbsent(rs.getString("INDEX_NAME"), k -> new ArrayList<>()).add(rs.getString("COLUMN_NAME"));
                }
                for (List<String> cols : byIndex.values()) {
                    if (cols.size() == 1 && !(pk.size() == 1 && pk.contains(cols.get(0)))) unique.add(cols.get(0));
                }
            }

            boolean hasNotes = note.keySet().stream().anyMatch(k -> !k.isEmpty());
            out.append(hasNotes ? "| カラム | 型 | 制約 | 意味 |\n|---|---|---|---|\n" : "| カラム | 型 | 制約 |\n|---|---|---|\n");
            try (ResultSet rs = md.getColumns(null, "PUBLIC", table, null)) {
                while (rs.next()) {
                    String col = rs.getString("COLUMN_NAME");
                    List<String> cons = new ArrayList<>();
                    if (pk.contains(col)) cons.add(pk.size() > 1 ? "主キー（複合）" : "主キー");
                    if ("YES".equals(rs.getString("IS_AUTOINCREMENT"))) cons.add("自動採番");
                    if (fk.containsKey(col)) cons.add("外部キー → `" + fk.get(col) + "`");
                    if (unique.contains(col)) cons.add("UNIQUE");
                    if (rs.getInt("NULLABLE") == DatabaseMetaData.columnNoNulls && !pk.contains(col)) cons.add("NOT NULL");
                    String def = rs.getString("COLUMN_DEF");
                    if (def != null && !"YES".equals(rs.getString("IS_AUTOINCREMENT"))) cons.add("既定値 " + def);
                    out.append("| `").append(col).append("` | `").append(type(rs)).append("` | ")
                       .append(String.join("、", cons)).append(" |");
                    if (hasNotes) out.append(' ').append(cell(note.getOrDefault(col, ""))).append(" |");
                    out.append('\n');
                }
            }

            // 初期データ
            int count;
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM \"" + table + "\"")) {
                rs.next(); count = rs.getInt(1);
            }
            out.append("\n");
            if (count == 0) {
                out.append("初期データはありません（演習のコードが登録します）。\n\n");
                continue;
            }
            out.append("初期データ（").append(count).append(" 件").append(count > MAX_ROWS ? "。先頭 " + MAX_ROWS + " 件" : "").append("）\n\n");
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM \"" + table + "\"")) {
                ResultSetMetaData rm = rs.getMetaData();
                int n = rm.getColumnCount();
                StringBuilder head = new StringBuilder("|"), sep = new StringBuilder("|");
                for (int i = 1; i <= n; i++) { head.append(' ').append(rm.getColumnName(i)).append(" |"); sep.append("---|"); }
                out.append(head).append('\n').append(sep).append('\n');
                int shown = 0;
                while (rs.next() && shown++ < MAX_ROWS) {
                    out.append('|');
                    for (int i = 1; i <= n; i++) {
                        Object v = rs.getObject(i);
                        out.append(' ').append(v == null ? "*(NULL)*" : cell(shorten(String.valueOf(v)))).append(" |");
                    }
                    out.append('\n');
                }
            }
            out.append('\n');
        }
        return out.toString();
    }

    static boolean exists(DatabaseMetaData md, String table) {
        try (ResultSet rs = md.getTables(null, "PUBLIC", table, null)) { return rs.next(); }
        catch (SQLException e) { return false; }
    }

    /** メタデータの型名を、DDL で見かける書き方に寄せる */
    static String type(ResultSet rs) throws SQLException {
        String t = rs.getString("TYPE_NAME").toUpperCase();
        int size = rs.getInt("COLUMN_SIZE");
        int scale = rs.getInt("DECIMAL_DIGITS");
        switch (t) {
            case "CHARACTER VARYING": case "VARCHAR": return "VARCHAR(" + size + ")";
            case "CHARACTER": case "CHAR": return "CHAR(" + size + ")";
            case "DECIMAL": case "NUMERIC": return t + "(" + size + (scale > 0 ? ", " + scale : "") + ")";
            case "INTEGER": return "INT";
            default: return t;
        }
    }

    static String shorten(String s) { return s.length() > 40 ? s.substring(0, 39) + "…" : s; }
    static String cell(String s) { return s.replace("|", "\\|").replace("\n", " ").replace("\t", " ").strip(); }
}
