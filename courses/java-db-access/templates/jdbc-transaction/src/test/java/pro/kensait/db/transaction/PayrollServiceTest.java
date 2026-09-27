package pro.kensait.db.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
 * 給与のテスト
 */
class PayrollServiceTest {
    private JDBCDataSource dataSource;
    private PayrollService service;

    // 各テストケースで共通的な前処理
    @BeforeEach
    void setUp() throws Exception {
        dataSource = new JDBCDataSource();
        dataSource.setUrl("jdbc:hsqldb:mem:transaction_course");
        dataSource.setUser("SA");
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP PROCEDURE COUNT_EMPLOYEES IF EXISTS");
            statement.executeUpdate("DROP TABLE EMPLOYEE IF EXISTS");
            statement.executeUpdate("CREATE TABLE EMPLOYEE (EMPLOYEE_ID INTEGER PRIMARY KEY, "
                    + "DEPARTMENT_ID INTEGER, EMPLOYEE_NAME VARCHAR(100) NOT NULL, "
                    + "SALARY DECIMAL(12, 2) NOT NULL CHECK (SALARY >= 0))");
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
            statement.executeUpdate("CREATE PROCEDURE COUNT_EMPLOYEES(IN P_DEPARTMENT_ID INTEGER, "
                    + "OUT P_TOTAL INTEGER) READS SQL DATA BEGIN ATOMIC SET P_TOTAL = "
                    + "(SELECT COUNT(*) FROM EMPLOYEE WHERE DEPARTMENT_ID = P_DEPARTMENT_ID); END");
        }
        service = new PayrollService(dataSource);
    }

    // 「一括処理のコミットとストアドプロシージャ呼び出し」の検証
    @Test
    void commitsBatchAndCallsStoredProcedure() throws Exception {
        Map<Integer, BigDecimal> raises = new LinkedHashMap<>();
        raises.put(10004, new BigDecimal("10000"));
        raises.put(10015, new BigDecimal("20000"));
        assertArrayEquals(new int[] {1, 1}, service.applyRaises(raises));
        assertEquals(new BigDecimal("410000.00"), salaryOf(10004));
        assertEquals(new BigDecimal("400000.00"), salaryOf(10015));
        assertEquals(5, service.countEmployees(3));
        assertEquals(0, service.countEmployees(99));
    }

    // 「制約違反時の一括処理全体のロールバック」の検証
    @Test
    void rollsBackWholeBatchOnConstraintViolation() throws Exception {
        Map<Integer, BigDecimal> raises = new LinkedHashMap<>();
        raises.put(10004, new BigDecimal("10000"));
        raises.put(10015, new BigDecimal("-999999"));
        assertThrows(SQLException.class, () -> service.applyRaises(raises));
        assertEquals(new BigDecimal("400000.00"), salaryOf(10004));
        assertEquals(new BigDecimal("380000.00"), salaryOf(10015));
    }

    // 「セーブポイントまでの限定ロールバック」の検証
    @Test
    void rollsBackOnlyToSavepoint() throws Exception {
        service.updateNameButCancelSalary(10004, "David", new BigDecimal("500000"));
        assertEquals("David", nameOf(10004));
        assertEquals(new BigDecimal("400000.00"), salaryOf(10004));
    }

    // 「社員未存在時の一括処理全体のロールバック」の検証
    @Test
    void rollsBackWholeBatchWhenEmployeeIsMissing() throws Exception {
        Map<Integer, BigDecimal> raises = new LinkedHashMap<>();
        raises.put(10004, new BigDecimal("10000"));
        raises.put(999, new BigDecimal("20000"));
        assertThrows(SQLException.class, () -> service.applyRaises(raises));
        assertEquals(new BigDecimal("400000.00"), salaryOf(10004));
    }

    // 「月給更新失敗時の氏名更新のロールバック」の検証
    @Test
    void rollsBackNameWhenSalaryUpdateFails() throws Exception {
        assertThrows(SQLException.class, () -> service.updateNameButCancelSalary(
                10004, "取り消される名前", new BigDecimal("-1")));
        assertEquals("Dave", nameOf(10004));
        assertEquals(new BigDecimal("400000.00"), salaryOf(10004));
    }

    // 「セーブポイント設定前の未存在社員の拒否」の検証
    @Test
    void rejectsMissingEmployeeBeforeSavepoint() {
        assertThrows(SQLException.class,
                () -> service.updateNameButCancelSalary(999, "不在", BigDecimal.ZERO));
    }

    // 月給のの実行
    private BigDecimal salaryOf(int id) throws Exception {
        return query(id, "SALARY", ResultSet::getBigDecimal);
    }

    // 名称のの実行
    private String nameOf(int id) throws Exception {
        return query(id, "EMPLOYEE_NAME", ResultSet::getString);
    }

    // 給与の検索
    private <T> T query(int id, String column, SqlReader<T> reader) throws Exception {
        try (Connection connection = dataSource.getConnection();
                var statement = connection.prepareStatement(
                        "SELECT EMPLOYEE_NAME, SALARY FROM EMPLOYEE WHERE EMPLOYEE_ID = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                return reader.read(resultSet, column);
            }
        }
    }

    /*
     * SQLreaderの契約を定義するインターフェース
     */
    @FunctionalInterface
    private interface SqlReader<T> {
        // SQLreaderの取得
        T read(ResultSet resultSet, String column) throws Exception;
    }
}
