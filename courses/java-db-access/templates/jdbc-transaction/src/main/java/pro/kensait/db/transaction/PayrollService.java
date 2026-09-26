package pro.kensait.db.transaction;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Types;
import java.util.Map;
import javax.sql.DataSource;

/*
 * 給与機能のビジネスロジック
 */
public final class PayrollService {
    private final ConnectionProvider connectionProvider;

    // 給与の初期化
    public PayrollService(DataSource dataSource) {
        connectionProvider = dataSource::getConnection;
    }

    // raisesの適用
    public int[] applyRaises(Map<Integer, BigDecimal> raises) throws SQLException {
        String sql = "UPDATE EMPLOYEE SET SALARY = SALARY + ? WHERE EMPLOYEE_ID = ?";
        try (Connection connection = connectionProvider.open()) {
            connection.setAutoCommit(false);
            try {
                int[] counts;
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    for (Map.Entry<Integer, BigDecimal> entry : raises.entrySet()) {
                        statement.setBigDecimal(1, entry.getValue());
                        statement.setInt(2, entry.getKey());
                        statement.addBatch();
                    }
                    counts = statement.executeBatch();
                }
                verifyBatchCounts(counts);
                connection.commit();
                return counts;
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            }
        }
    }

    // 名称butcancel月給の更新
    public void updateNameButCancelSalary(int employeeId, String name, BigDecimal salary)
            throws SQLException {
        try (Connection connection = connectionProvider.open()) {
            connection.setAutoCommit(false);
            try {
                updateName(connection, employeeId, name);
                Savepoint beforeSalary = connection.setSavepoint("before_salary");
                updateSalary(connection, employeeId, salary);
                connection.rollback(beforeSalary);
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            }
        }
    }

    // 件数社員の実行
    public int countEmployees(int departmentId) throws SQLException {
        try (Connection connection = connectionProvider.open();
                CallableStatement statement = connection.prepareCall("{call COUNT_EMPLOYEES(?, ?)}")) {
            statement.setInt(1, departmentId);
            statement.registerOutParameter(2, Types.INTEGER);
            statement.execute();
            return statement.getInt(2);
        }
    }

    // 名称の更新
    private static void updateName(Connection connection, int id, String name) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE EMPLOYEE SET EMPLOYEE_NAME = ? WHERE EMPLOYEE_ID = ?")) {
            statement.setString(1, name);
            statement.setInt(2, id);
            requireOneRow(statement.executeUpdate());
        }
    }

    // 月給の更新
    private static void updateSalary(Connection connection, int id, BigDecimal salary)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE EMPLOYEE SET SALARY = ? WHERE EMPLOYEE_ID = ?")) {
            statement.setBigDecimal(1, salary);
            statement.setInt(2, id);
            requireOneRow(statement.executeUpdate());
        }
    }

    // バッチcountsの検証
    private static void verifyBatchCounts(int[] counts) throws SQLException {
        for (int count : counts) {
            // 成功でも件数不明の場合がある-2を「更新失敗」と誤解しない
            if (count != Statement.SUCCESS_NO_INFO) {
                requireOneRow(count);
            }
        }
    }

    // require1件行の実行
    private static void requireOneRow(int count) throws SQLException {
        if (count != 1) {
            throw new SQLException("Expected one employee to be updated, but got " + count);
        }
    }

    // rollbackの実行
    private static void rollback(Connection connection, Exception original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            original.addSuppressed(rollbackFailure);
        }
    }

    /*
     * 接続providerの契約を定義するインターフェース
     */
    @FunctionalInterface
    private interface ConnectionProvider {
        // 接続providerの表示
        Connection open() throws SQLException;
    }
}
