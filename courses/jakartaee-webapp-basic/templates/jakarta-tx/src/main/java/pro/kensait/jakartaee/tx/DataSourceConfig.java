package pro.kensait.jakartaee.tx;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/** データソース java:app/jdbc/TxDS（同梱の HSQLDB のインメモリ DB）。既定でトランザクションに参加する */
@DataSourceDefinition(
        name = "java:app/jdbc/TxDS",
        className = "org.hsqldb.jdbc.JDBCDataSource",
        url = "jdbc:hsqldb:mem:testdb",
        user = "SA",
        password = "")
@WebListener
public class DataSourceConfig implements ServletContextListener {
}
