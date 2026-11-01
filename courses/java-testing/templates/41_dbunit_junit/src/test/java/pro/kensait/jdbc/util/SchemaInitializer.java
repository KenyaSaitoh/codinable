package pro.kensait.jdbc.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

/*
 * テストの実行の最初に 1 回だけ、インメモリの HSQLDB に schema.sql のテーブルを作る
 * （講座本編の HSQLDB サーバーを起動してテーブルを作る手順の代わり）
 * META-INF/services に登録してあるので、テストクラスには何も書かなくてよい
 */
public class SchemaInitializer implements LauncherSessionListener {
    private static boolean done;

    @Override
    public synchronized void launcherSessionOpened(LauncherSession session) {
        if (done) return;
        done = true;
        try (InputStream in = getClass().getResourceAsStream("/schema.sql");
                Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:testdb", "SA", "");
                Statement stmt = conn.createStatement()) {
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8)
                    .replaceAll("(?m)--.*$", "");
            for (String one : sql.split(";")) {
                if (!one.isBlank()) stmt.execute(one);
            }
        } catch (Exception e) {
            throw new IllegalStateException("schema.sql を流せません", e);
        }
    }
}
