// 演習の .sql が実際に HSQLDB で流れるかを確かめる検査用プログラム
//
// 文の分割は main/sql.js (SqlServer) と同じ「; で split して trim」にしてある
// アプリと同じ切り方で試さないと、コメント中の ; などの取りこぼしが見つからない
//
//   java -cp <hsqldb.jar> SqlCheck.java <expect> <file.sql> ...
//     expect = ok       … 全文が成功すること
//     expect = fail     … どこかで失敗すること (制約違反を見せる教材)
//     expect = eachfail … 1 文ずつ流したとき、すべてが失敗すること
import java.nio.file.*;
import java.sql.*;

public class SqlCheck {
    public static void main(String[] args) throws Exception {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        int ng = 0;

        for (int a = 0; a < args.length; a += 2) {
            String expect = args[a];
            String file   = args[a + 1];
            String sql    = new String(Files.readAllBytes(Paths.get(file)), "UTF-8");

            // 演習は 1 つの DB を共有して順に流すので、毎回つなぎ直す
            try (Connection conn = DriverManager.getConnection(
                     "jdbc:hsqldb:mem:check;DB_CLOSE_DELAY=-1", "SA", "")) {
                String  error   = null;
                int     stmts   = 0;
                boolean each    = expect.equals("eachfail");
                for (String part : sql.split(";")) {
                    String p = part.trim();
                    if (p.isEmpty()) continue;
                    stmts++;
                    try (Statement s = conn.createStatement()) {
                        s.execute(p);
                        // 1 文ずつ全部失敗してほしい教材で、成功してしまった
                        if (each) {
                            error = "文" + stmts + ": 失敗するはずが成功した"
                                  + "\n      -> " + firstLine(p);
                            break;
                        }
                    } catch (SQLException e) {
                        if (each) continue;      // 期待どおりなので次の文へ
                        error = "文" + stmts + ": " + e.getMessage()
                              + "\n      -> " + firstLine(p);
                        break;
                    }
                }
                if (each) {
                    boolean allFailed = error == null;
                    if (!allFailed) ng++;
                    System.out.println((allFailed ? "OK   " : "NG   ") + file
                        + "  (" + stmts + " 文すべて失敗すること)"
                        + (error != null ? "\n     " + error : ""));
                    continue;
                }
                boolean ok = expect.equals("fail") ? error != null : error == null;
                if (!ok) ng++;
                System.out.println((ok ? "OK   " : "NG   ") + file
                    + "  (" + stmts + " 文, expect=" + expect + ")"
                    + (error != null ? "\n     " + error : ""));
            }
        }
        System.out.println(ng == 0 ? "すべて期待どおり" : ("NG " + ng + " 件"));
        System.exit(ng == 0 ? 0 : 1);
    }

    static String firstLine(String sql) {
        for (String line : sql.split("\n")) {
            String t = line.trim();
            if (!t.isEmpty() && !t.startsWith("--")) return t;
        }
        return sql.trim();
    }
}
