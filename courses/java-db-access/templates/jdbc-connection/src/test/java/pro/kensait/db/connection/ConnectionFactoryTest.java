package pro.kensait.db.connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;

/*
 * 接続ファクトリーのテスト
 */
class ConnectionFactoryTest {
    private static final String URL = "jdbc:hsqldb:mem:connection_course";

    // 「JDBC URLの解析」の検証
    @Test
    void parsesJdbcUrl() {
        JdbcUrl parsed = JdbcUrl.parse(URL);
        assertEquals("hsqldb", parsed.subprotocol());
        assertEquals("mem:connection_course", parsed.subname());
    }

    // 「構成要素が不足したJDBC URLの拒否」の検証
    @Test
    void rejectsMissingJdbcUrlComponents() {
        for (String value : new String[] {null, "", "hsqldb:mem:test", "jdbc:hsqldb",
                "jdbc:hsqldb:", "jdbc::mem:test"}) {
            assertThrows(IllegalArgumentException.class, () -> JdbcUrl.parse(value));
        }
    }

    // 「2種類のAPIによる接続確立」の検証
    @Test
    void opensConnectionsThroughBothApis() throws Exception {
        try (Connection connection = ConnectionFactory.openWithDriverManager(URL, "SA", "")) {
            assertEquals("HSQL Database Engine", connection.getMetaData().getDatabaseProductName());
            assertTrue(connection.getMetaData().supportsTransactions());
            assertTrue(connection.getAutoCommit());
        }

        DataSource dataSource = ConnectionFactory.hsqldbDataSource(URL, "SA", "");
        try (Connection connection = dataSource.getConnection()) {
            assertTrue(connection.isValid(1));
        }
    }
}
