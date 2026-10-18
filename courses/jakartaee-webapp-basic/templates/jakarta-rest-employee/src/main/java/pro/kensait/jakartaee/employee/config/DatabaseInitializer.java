package pro.kensait.jakartaee.employee.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import javax.sql.DataSource;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * 演習用: アプリケーションの起動時に、テーブルを作り直して初期データを入れる
 * （元の SQL は sql/hsqldb にあり、ビルドで 1 行 1 文に整えて WAR の META-INF/sql に入る）
 * デプロイのたびに初期データへ戻るので、前に動かした結果は残らない
 */
@WebListener
public class DatabaseInitializer implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(DatabaseInitializer.class.getName());
    private static final String[] SCRIPTS = {
            "1_EMPLOYEE_DROP.sql", "2_EMPLOYEE_DDL.sql", "3_EMPLOYEE_DML.sql"};

    @Resource(lookup = "java:app/jdbc/EmployeeDS")
    private DataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            for (String script : SCRIPTS) {
                for (String sql : read("META-INF/sql/" + script).split("\n")) {
                    if (!sql.isBlank()) {
                        statement.execute(sql);
                    }
                }
            }
            logger.info("テーブルと初期データを用意しました");
        } catch (SQLException | IOException exception) {
            throw new IllegalStateException("初期データを入れられません", exception);
        }
    }

    private String read(String path) throws IOException {
        try (InputStream input = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(path)) {
            if (input == null) {
                throw new IOException(path + " が見つかりません");
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
