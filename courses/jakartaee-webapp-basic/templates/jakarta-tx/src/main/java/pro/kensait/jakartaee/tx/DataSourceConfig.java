package pro.kensait.jakartaee.tx;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/** データソース java:app/jdbc/TxDS（Codinable が起こす HSQLDB サーバー localhost:9001 の testdb。中身はインメモリ）。既定でトランザクションに参加する */
@DataSourceDefinition(
        name = "java:app/jdbc/TxDS",
        className = "org.hsqldb.jdbc.JDBCDataSource",
        url = "jdbc:hsqldb:hsql://localhost:9001/testdb",
        user = "SA",
        password = "")
@WebListener
public class DataSourceConfig implements ServletContextListener {
}
