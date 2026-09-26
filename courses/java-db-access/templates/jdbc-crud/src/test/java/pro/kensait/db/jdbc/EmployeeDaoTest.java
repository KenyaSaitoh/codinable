package pro.kensait.db.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.SQLException;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
 * 社員daoのテスト
 */
class EmployeeDaoTest {
    private EmployeeDao dao;

    // 各テストケースで共通的な前処理
    @BeforeEach
    void setUp() throws Exception {
        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl("jdbc:hsqldb:mem:crud_course");
        dataSource.setUser("SA");
        dataSource.setPassword("");
        // 破壊的な初期化は、テスト専用インメモリDBのセットアップに限定する
        try (var connection = dataSource.getConnection();
                var statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE EMPLOYEE IF EXISTS");
            statement.executeUpdate("CREATE TABLE EMPLOYEE (EMPLOYEE_ID INTEGER PRIMARY KEY, "
                    + "DEPARTMENT_ID INTEGER NOT NULL, EMPLOYEE_NAME VARCHAR(100) NOT NULL, "
                    + "SALARY DECIMAL(12, 2) NOT NULL)");
        }
        dao = new EmployeeDao(dataSource);
    }

    // 「バインドパラメータによるCRUD」の検証
    @Test
    void performsCrudWithBoundParameters() throws Exception {
        Employee original = new Employee(101, 10, "佐藤 花子", new BigDecimal("420000.00"));
        assertEquals(1, dao.insert(original));
        assertEquals(original, dao.findById(101).orElseThrow());

        Employee changed = new Employee(101, 20, "佐藤 花子", new BigDecimal("450000.00"));
        assertEquals(1, dao.update(changed));
        assertEquals(changed, dao.findById(101).orElseThrow());

        assertEquals(1, dao.delete(101));
        assertTrue(dao.findById(101).isEmpty());
    }

    // 「氏名内のSQL構文のデータ扱い」の検証
    @Test
    void treatsSqlSyntaxInNameAsData() throws Exception {
        Employee employee = new Employee(101, 10, "O'Brien'); DROP TABLE EMPLOYEE; --",
                new BigDecimal("420000.00"));
        assertEquals(1, dao.insert(employee));
        assertEquals(employee, dao.findById(101).orElseThrow());
    }

    // 「他社員を変更しない未存在行の通知」の検証
    @Test
    void reportsMissingRowsWithoutChangingOtherEmployees() throws Exception {
        assertTrue(dao.findById(999).isEmpty());
        assertEquals(0, dao.update(new Employee(999, 10, "不在", BigDecimal.ZERO)));
        assertEquals(0, dao.delete(999));
    }

    // 「確定済み社員を上書きしないID重複エラー」の検証
    @Test
    void duplicateIdFailsWithoutOverwritingCommittedEmployee() throws Exception {
        Employee original = new Employee(101, 10, "佐藤 花子", new BigDecimal("420000.00"));
        dao.insert(original);
        assertThrows(SQLException.class,
                () -> dao.insert(new Employee(101, 20, "別の社員", BigDecimal.ZERO)));
        assertEquals(original, dao.findById(101).orElseThrow());
    }
}
