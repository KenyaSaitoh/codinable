package pro.kensait.jakartaee.employee.security;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.logging.Logger;
import javax.sql.DataSource;

/*
 * アプリケーションの起動時に、教材用の 2 つのアカウントを登録する
 * テーブルはデプロイのたびに作り直す（前に実行したときの利用者を残さない）
 */
@ApplicationScoped
public class UserInitializer {
    private static final Logger logger = Logger.getLogger(UserInitializer.class.getName());

    @Resource(lookup = "java:comp/DefaultDataSource")
    private DataSource dataSource;
    @Inject
    private Pbkdf2PasswordHash passwordHash;

    public void init(@Observes Startup event) throws SQLException { // 【1】
        createTables();
        passwordHash.initialize(Map.of("Pbkdf2PasswordHash.Iterations", "600000")); // 【2】
        // 講座専用のテストアカウント
        insertUser("Alice", passwordHash.generate("1111".toCharArray()), "BASIC"); // 【3】
        insertUser("Bob", passwordHash.generate("2222".toCharArray()), "ADMIN");
        logger.info("[ UserInitializer#init ] APP_USER に Alice（BASIC）と Bob（ADMIN）を登録しました");
    }

    private void createTables() throws SQLException {
        try (Connection con = dataSource.getConnection(); Statement st = con.createStatement()) {
            st.executeUpdate("DROP TABLE IF EXISTS APP_USER_GROUP");
            st.executeUpdate("DROP TABLE IF EXISTS APP_USER");
            st.executeUpdate("CREATE TABLE APP_USER (USER_NAME VARCHAR(30) PRIMARY KEY, "
                    + "PASSWORD_HASH VARCHAR(255) NOT NULL)");
            st.executeUpdate("CREATE TABLE APP_USER_GROUP (USER_NAME VARCHAR(30) NOT NULL, "
                    + "GROUP_NAME VARCHAR(30) NOT NULL, PRIMARY KEY (USER_NAME, GROUP_NAME))");
        }
    }

    private void insertUser(String name, String hash, String group) throws SQLException {
        // ハッシュ値だけを保存し、平文のパスワードはログにも出さない
        try (Connection con = dataSource.getConnection();
                PreparedStatement user = con.prepareStatement(
                        "INSERT INTO APP_USER (USER_NAME, PASSWORD_HASH) VALUES (?, ?)");
                PreparedStatement userGroup = con.prepareStatement(
                        "INSERT INTO APP_USER_GROUP (USER_NAME, GROUP_NAME) VALUES (?, ?)")) {
            user.setString(1, name);
            user.setString(2, hash);
            user.executeUpdate();
            userGroup.setString(1, name);
            userGroup.setString(2, group);
            userGroup.executeUpdate();
        }
    }
}
