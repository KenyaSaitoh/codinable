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
                    + "DEPARTMENT_ID INTEGER, EMPLOYEE_NAME VARCHAR(100) NOT NULL, "
                    + "SALARY DECIMAL(12, 2) NOT NULL)");
            // 初期データ16人（部署ID 1=PLANNING / 2=HR / 3=SALES / 4=PRODUCT、Victorは未所属）
            statement.executeUpdate("INSERT INTO EMPLOYEE VALUES "
                    + "(10001, 3, 'Alice', 500000), "
                    + "(10002, 1, 'Bob', 450000), "
                    + "(10003, 2, 'Carol', 350000), "
                    + "(10004, 3, 'Dave', 400000), "
                    + "(10005, 3, 'Ellen', 300000), "
                    + "(10006, 1, 'Frank', 250000), "
                    + "(10007, 4, 'Ivan', 480000), "
                    + "(10008, 2, 'Justin', 460000), "
                    + "(10009, 4, 'Mallory', 420000), "
                    + "(10010, 3, 'Matilda', 280000), "
                    + "(10011, 4, 'Oscar', 320000), "
                    + "(10012, 4, 'Pat', 240000), "
                    + "(10013, 3, 'Peggy', 270000), "
                    + "(10014, NULL, 'Victor', 220000), "
                    + "(10015, 1, 'Steve', 380000), "
                    + "(10016, 4, 'Trent', 310000)");
        }
        dao = new EmployeeDao(dataSource);
    }

    // 「バインドパラメータによるCRUD」の検証
    @Test
    void performsCrudWithBoundParameters() throws Exception {
        Employee original = new Employee(10017, 3, "Walter", new BigDecimal("230000.00"));
        assertEquals(1, dao.insert(original));
        assertEquals(original, dao.findById(10017).orElseThrow());

        Employee changed = new Employee(10017, 4, "Walter", new BigDecimal("260000.00"));
        assertEquals(1, dao.update(changed));
        assertEquals(changed, dao.findById(10017).orElseThrow());

        assertEquals(1, dao.delete(10017));
        assertTrue(dao.findById(10017).isEmpty());
    }

    // 「氏名内のSQL構文のデータ扱い」の検証
    @Test
    void treatsSqlSyntaxInNameAsData() throws Exception {
        Employee employee = new Employee(10017, 3, "O'Brien'); DROP TABLE EMPLOYEE; --",
                new BigDecimal("420000.00"));
        assertEquals(1, dao.insert(employee));
        assertEquals(employee, dao.findById(10017).orElseThrow());
    }

    // 「他社員を変更しない未存在行の通知」の検証
    @Test
    void reportsMissingRowsWithoutChangingOtherEmployees() throws Exception {
        assertTrue(dao.findById(999).isEmpty());
        assertEquals(0, dao.update(new Employee(999, 3, "不在", BigDecimal.ZERO)));
        assertEquals(0, dao.delete(999));
    }

    // 「確定済み社員を上書きしないID重複エラー」の検証
    @Test
    void duplicateIdFailsWithoutOverwritingCommittedEmployee() throws Exception {
        Employee original = new Employee(10017, 3, "Walter", new BigDecimal("230000.00"));
        dao.insert(original);
        assertThrows(SQLException.class,
                () -> dao.insert(new Employee(10017, 4, "別の社員", BigDecimal.ZERO)));
        assertEquals(original, dao.findById(10017).orElseThrow());
    }
}
