package pro.kensait.jakartaee.employee.config;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * アプリケーションの中で DataSource を定義する（接続先は同梱の HSQLDB のインメモリ DB）
 * @DataSourceDefinition はコンテナが管理するクラスに付けるため、中身のない @WebListener のクラスに置く
 */
@DataSourceDefinition(
        name = "java:app/jdbc/EmployeeDS",
        className = "org.hsqldb.jdbc.JDBCDataSource",
        url = "jdbc:hsqldb:mem:testdb",
        user = "SA",
        password = "",
        minPoolSize = 2,
        maxPoolSize = 10)
@WebListener
public class DataSourceConfig implements ServletContextListener {
}
