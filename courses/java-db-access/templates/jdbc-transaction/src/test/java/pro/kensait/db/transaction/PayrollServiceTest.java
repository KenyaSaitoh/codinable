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
                    + "DEPARTMENT_ID INTEGER NOT NULL, EMPLOYEE_NAME VARCHAR(100) NOT NULL, "
                    + "SALARY DECIMAL(12, 2) NOT NULL CHECK (SALARY >= 0))");
            statement.executeUpdate("INSERT INTO EMPLOYEE VALUES (101, 10, '佐藤 花子', 400000)");
            statement.executeUpdate("INSERT INTO EMPLOYEE VALUES (102, 10, '鈴木 一郎', 380000)");
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
        raises.put(101, new BigDecimal("10000"));
        raises.put(102, new BigDecimal("20000"));
        assertArrayEquals(new int[] {1, 1}, service.applyRaises(raises));
        assertEquals(new BigDecimal("410000.00"), salaryOf(101));
        assertEquals(new BigDecimal("400000.00"), salaryOf(102));
        assertEquals(2, service.countEmployees(10));
        assertEquals(0, service.countEmployees(99));
    }

    // 「制約違反時の一括処理全体のロールバック」の検証
    @Test
    void rollsBackWholeBatchOnConstraintViolation() throws Exception {
        Map<Integer, BigDecimal> raises = new LinkedHashMap<>();
        raises.put(101, new BigDecimal("10000"));
        raises.put(102, new BigDecimal("-999999"));
        assertThrows(SQLException.class, () -> service.applyRaises(raises));
        assertEquals(new BigDecimal("400000.00"), salaryOf(101));
        assertEquals(new BigDecimal("380000.00"), salaryOf(102));
    }

    // 「セーブポイントまでの限定ロールバック」の検証
    @Test
    void rollsBackOnlyToSavepoint() throws Exception {
        service.updateNameButCancelSalary(101, "佐藤 華子", new BigDecimal("500000"));
        assertEquals("佐藤 華子", nameOf(101));
        assertEquals(new BigDecimal("400000.00"), salaryOf(101));
    }

    // 「社員未存在時の一括処理全体のロールバック」の検証
    @Test
    void rollsBackWholeBatchWhenEmployeeIsMissing() throws Exception {
        Map<Integer, BigDecimal> raises = new LinkedHashMap<>();
        raises.put(101, new BigDecimal("10000"));
        raises.put(999, new BigDecimal("20000"));
        assertThrows(SQLException.class, () -> service.applyRaises(raises));
        assertEquals(new BigDecimal("400000.00"), salaryOf(101));
    }

    // 「月給更新失敗時の氏名更新のロールバック」の検証
    @Test
    void rollsBackNameWhenSalaryUpdateFails() throws Exception {
        assertThrows(SQLException.class, () -> service.updateNameButCancelSalary(
                101, "取り消される名前", new BigDecimal("-1")));
        assertEquals("佐藤 花子", nameOf(101));
        assertEquals(new BigDecimal("400000.00"), salaryOf(101));
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
