package pro.kensait.db.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.hsqldb.jdbc.JDBCDataSource;
/** 接続生成方法の違いを並べて観察するためのファクトリです */
public final class ConnectionFactory {
    // 接続の初期化
    private ConnectionFactory() {
    }

    // drivermanagerの表示
    public static Connection openWithDriverManager(String url, String user, String password)
            throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    // hsqldbデータ変換元の実行
    public static DataSource hsqldbDataSource(String url, String user, String password) {
        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl(url);
        dataSource.setUser(user);
        dataSource.setPassword(password);
        return dataSource;
    }
}
