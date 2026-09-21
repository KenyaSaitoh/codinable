// ═══════════════════════════════════════════════════════════
//  SQL 実行エンジン (HSQLDB インメモリ)
//
//  講座の DB / SQL の章で「サーバーを別に立てずに SQL を試す」ための仕組み
//  同梱の HSQLDB jar を使い、常駐する小さな Java プロセス (SqlServer) と
//  標準入出力で 1 行 1 リクエストの JSON をやり取りする
//
//  SqlServer.java は初回だけコンパイルして userData にキャッシュする
// ═══════════════════════════════════════════════════════════

const fs   = require('fs');
const path = require('path');
const { app } = require('electron');
const { spawn, execFile } = require('child_process');

const {
  IS_WIN, resolveJavaTool, resolveHsqldb, getDevEnv,
  getJavaRuntimeOptions, getJavacRuntimeOptions,
} = require('./runtimes');
const { decodeOutput } = require('./util');

const SQL_SERVER_SOURCE = `
import java.sql.*;
import java.io.*;

public class SqlServer {
    private static Connection conn;

    public static void main(String[] args) throws Exception {
        Class.forName("org.hsqldb.jdbc.JDBCDriver");
        conn = DriverManager.getConnection(
            "jdbc:hsqldb:mem:sqldb;DB_CLOSE_DELAY=-1", "SA", "");

        PrintStream out = new PrintStream(
            new BufferedOutputStream(System.out), true, "UTF-8");
        BufferedReader in = new BufferedReader(
            new InputStreamReader(System.in, "UTF-8"));

        out.println("{\\"ready\\":true}");

        String line;
        while ((line = in.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;
            if (line.equals("EXIT")) break;

            String sql = extractField(line, "sql");
            if (sql == null) {
                out.println("{\\"error\\":\\"invalid request\\"}");
                continue;
            }
            try {
                out.println(execAll(sql));
            } catch (Exception e) {
                String msg = e.getMessage() != null
                    ? e.getMessage() : e.getClass().getSimpleName();
                out.println("{\\"error\\":\\"" + esc(msg) + "\\"}");
            }
        }
        try { conn.close(); } catch (Exception ignored) {}
    }

    static String execAll(String sql) throws SQLException {
        String[] parts = sql.split(";");
        String last = null;
        for (String p : parts) {
            p = p.trim();
            if (!p.isEmpty()) last = execOne(p);
        }
        return last != null ? last : "{\\"columns\\":[],\\"rows\\":[],\\"rowCount\\":0}";
    }

    static String execOne(String sql) throws SQLException {
        try (Statement s = conn.createStatement()) {
            if (s.execute(sql)) {
                ResultSet rs = s.getResultSet();
                ResultSetMetaData m = rs.getMetaData();
                int cols = m.getColumnCount();
                StringBuilder b = new StringBuilder("{\\"columns\\":[");
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) b.append(",");
                    b.append('"').append(esc(m.getColumnLabel(i))).append('"');
                }
                b.append("],\\"rows\\":[");
                boolean first = true;
                int n = 0;
                while (rs.next() && n < 500) {
                    if (!first) b.append(",");
                    first = false; n++;
                    b.append("[");
                    for (int i = 1; i <= cols; i++) {
                        if (i > 1) b.append(",");
                        Object v = rs.getObject(i);
                        if (v == null) b.append("null");
                        else b.append('"').append(esc(v.toString())).append('"');
                    }
                    b.append("]");
                }
                b.append("],\\"rowCount\\":").append(n).append("}");
                return b.toString();
            } else {
                return "{\\"affected\\":" + s.getUpdateCount() + "}";
            }
        }
    }

    static String extractField(String json, String key) {
        String needle = "\\"" + key + "\\":\\"";
        int i = json.indexOf(needle);
        if (i < 0) return null;
        int start = i + needle.length();
        StringBuilder sb = new StringBuilder();
        for (int j = start; j < json.length(); j++) {
            char c = json.charAt(j);
            if (c == '\\\\' && j + 1 < json.length()) {
                char n = json.charAt(++j);
                if      (n == '"')  sb.append('"');
                else if (n == 'n')  sb.append('\\n');
                else if (n == 'r')  sb.append('\\r');
                else if (n == 't')  sb.append('\\t');
                else if (n == '\\\\') sb.append('\\\\');
                else sb.append(n);
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    static String esc(String s) {
        return s.replace("\\\\", "\\\\\\\\")
                .replace("\\"",  "\\\\\\"")
                .replace("\\n",  "\\\\n")
                .replace("\\r",  "\\\\r")
                .replace("\\t",  "\\\\t");
    }
}
`;

let serverProcess  = null;
let outputBuf      = '';
let pendingResolve = null;

function runnerDir() {
  return path.join(app.getPath('userData'), 'sql-runner');
}

function compileServer(uiLang) {
  const dir = runnerDir();
  fs.mkdirSync(dir, { recursive: true });
  const javaFile = path.join(dir, 'SqlServer.java');
  fs.writeFileSync(javaFile, SQL_SERVER_SOURCE.trimStart(), 'utf8');

  const hsqlJar = resolveHsqldb();
  const cpArg   = hsqlJar ? ['-cp', hsqlJar] : [];

  return new Promise((resolve, reject) => {
    execFile(resolveJavaTool('javac'),
      [...getJavacRuntimeOptions(uiLang), ...cpArg, '-d', dir, javaFile],
      { timeout: 60_000, encoding: 'buffer' }, (err, _out, stderr) => {
        if (err) reject(new Error('SqlServer compile: ' + decodeOutput(stderr)));
        else resolve();
      });
  });
}

function onStdout(chunk) {
  outputBuf += decodeOutput(chunk);
  let nl;
  while ((nl = outputBuf.indexOf('\n')) >= 0) {
    const line = outputBuf.slice(0, nl).trim();
    outputBuf = outputBuf.slice(nl + 1);
    if (!line) continue;
    if (pendingResolve) {
      const resolve = pendingResolve;
      pendingResolve = null;
      try { resolve(JSON.parse(line)); } catch { resolve({ error: 'parse: ' + line }); }
    }
  }
}

function request(sql) {
  return new Promise((resolve, reject) => {
    if (!serverProcess) { reject(new Error('HSQLDB not running')); return; }
    const timer = setTimeout(() => {
      pendingResolve = null;
      reject(new Error('SQL timeout'));
    }, 30_000);
    pendingResolve = result => { clearTimeout(timer); resolve(result); };
    serverProcess.stdin.write(JSON.stringify({ sql }) + '\n');
  });
}

function stop() {
  if (serverProcess) {
    try { serverProcess.stdin.write('EXIT\n'); } catch { /* すでに閉じている */ }
    try { serverProcess.kill(); } catch { /* すでに終了している */ }
    serverProcess = null;
  }
  outputBuf = '';
  pendingResolve = null;
  return { ok: true };
}

/**
 * DB を起動する (稼働中なら PUBLIC スキーマを作り直すだけで済ませる)
 * schemaSQL を渡すと初期化 SQL として流す
 */
async function start({ schemaSQL, uiLang } = {}) {
  if (serverProcess) {
    try {
      const dropped = await request('DROP SCHEMA PUBLIC CASCADE');
      if (dropped && dropped.error) throw new Error(dropped.error);
      if (schemaSQL && schemaSQL.trim()) {
        const loaded = await request(schemaSQL);
        if (loaded && loaded.error) throw new Error(loaded.error);
      }
      return { ok: true, reused: true };
    } catch (err) {
      console.warn('[sql] in-place reset failed, restarting HSQLDB:', err.message);
    }
  }

  try {
    stop();
    const dir = runnerDir();
    if (!fs.existsSync(path.join(dir, 'SqlServer.class'))) await compileServer(uiLang);

    const hsqlJar = resolveHsqldb();
    if (!hsqlJar) return { ok: false, error: 'HSQLDB jar が見つかりません (hsqldb/ が未セットアップです)' };

    const classpath = dir + (IS_WIN ? ';' : ':') + hsqlJar;
    outputBuf = '';

    // spawn する前に ready の受け口を用意しておく (取りこぼし防止)
    const ready = new Promise((resolve, reject) => {
      const timer = setTimeout(() => {
        pendingResolve = null;
        reject(new Error('HSQLDB の起動がタイムアウトしました'));
      }, 20_000);
      pendingResolve = result => { clearTimeout(timer); resolve(result); };
    });

    serverProcess = spawn(resolveJavaTool('java'),
      [...getJavaRuntimeOptions(uiLang), '-cp', classpath, 'SqlServer'],
      { env: getDevEnv(), windowsHide: true });
    serverProcess.stdout.on('data', onStdout);
    serverProcess.stderr.on('data', () => { /* HSQLDB の起動ログは捨てる */ });
    serverProcess.on('close', () => { serverProcess = null; });

    await ready;

    if (schemaSQL && schemaSQL.trim()) {
      const loaded = await request(schemaSQL);
      if (loaded && loaded.error) return { ok: false, error: loaded.error };
    }
    return { ok: true };
  } catch (err) {
    stop();
    return { ok: false, error: err.message };
  }
}

async function run(sql) {
  if (!serverProcess) return { error: 'HSQLDB が起動していません。「DB起動」を押してください。' };
  try { return await request(sql); }
  catch (err) { return { error: err.message }; }
}

function isRunning() {
  return !!serverProcess;
}

module.exports = { start, run, stop, isRunning };
