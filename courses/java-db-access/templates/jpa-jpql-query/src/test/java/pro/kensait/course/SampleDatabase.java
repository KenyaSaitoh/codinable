package pro.kensait.course;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
/** 教材のDDLと初期データを、テスト専用DBへ投入する */
public final class SampleDatabase {
    // sampleデータベースの初期化
    private SampleDatabase() {
    }

    // sampleデータベースの初期化
    public static void reset() throws Exception {
        String url = "jdbc:hsqldb:mem:" + System.getProperty("sample.project");
        try (var connection = DriverManager.getConnection(url, "SA", "");
                var statement = connection.createStatement();
                var paths = Files.list(Path.of(System.getProperty("sample.sql.dir")))) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".sql")).sorted().toList()) {
                String sql = Files.readString(path).replaceAll("(?m)--[^\\r\\n]*", "");
                for (String command : sql.split(";")) {
                    if (!command.isBlank()) {
                        statement.execute(command);
                    }
                }
            }
        }
    }
}
