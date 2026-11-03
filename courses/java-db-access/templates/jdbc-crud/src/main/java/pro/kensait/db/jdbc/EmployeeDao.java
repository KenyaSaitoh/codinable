package pro.kensait.db.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/*
 * 社員情報の永続化を担うリポジトリ
 */
public final class EmployeeDao {
    private final ConnectionProvider connectionProvider;

    // 社員の初期化
    public EmployeeDao(DataSource dataSource) {
        connectionProvider = dataSource::getConnection;
    }

    // 社員の登録
    public int insert(Employee employee) throws SQLException {
        String sql = "INSERT INTO EMPLOYEE "
                + "(EMPLOYEE_ID, DEPARTMENT_ID, EMPLOYEE_NAME, SALARY) VALUES (?, ?, ?, ?)";
        return executeUpdate(sql, statement -> {
            statement.setInt(1, employee.id());
            statement.setInt(2, employee.departmentId());
            statement.setString(3, employee.name());
            statement.setBigDecimal(4, employee.salary());
        });
    }

    // リポジトリメソッド：主キー検索によって社員の取得
    public Optional<Employee> findById(int id) throws SQLException {
        String sql = "SELECT EMPLOYEE_ID, DEPARTMENT_ID, EMPLOYEE_NAME, SALARY "
                + "FROM EMPLOYEE WHERE EMPLOYEE_ID = ?";
        try (Connection connection = connectionProvider.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(resultSet));
            }
        }
    }

    // リポジトリメソッド：条件検索によって部署の社員を社員番号順に取得
    public List<Employee> findByDepartmentId(int departmentId) throws SQLException {
        String sql = "SELECT EMPLOYEE_ID, DEPARTMENT_ID, EMPLOYEE_NAME, SALARY "
                + "FROM EMPLOYEE WHERE DEPARTMENT_ID = ? ORDER BY EMPLOYEE_ID";
        try (Connection connection = connectionProvider.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, departmentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Employee> employees = new ArrayList<>();
                while (resultSet.next()) {
                    employees.add(map(resultSet));
                }
                return employees;
            }
        }
    }

    // 一件更新
    public int update(Employee employee) throws SQLException {
        String sql = "UPDATE EMPLOYEE SET DEPARTMENT_ID = ?, EMPLOYEE_NAME = ?, SALARY = ? "
                + "WHERE EMPLOYEE_ID = ?";
        return executeUpdate(sql, statement -> {
            statement.setInt(1, employee.departmentId());
            statement.setString(2, employee.name());
            statement.setBigDecimal(3, employee.salary());
            statement.setInt(4, employee.id());
        });
    }

    // 一括更新：部署の全社員の給与を加算し、更新件数を返す
    public int raiseSalaryByDepartmentId(int departmentId, BigDecimal amount) throws SQLException {
        String sql = "UPDATE EMPLOYEE SET SALARY = SALARY + ? WHERE DEPARTMENT_ID = ?";
        return executeUpdate(sql, statement -> {
            statement.setBigDecimal(1, amount);
            statement.setInt(2, departmentId);
        });
    }

    // APIメソッド：Employeeの削除
    public int delete(int id) throws SQLException {
        return executeUpdate("DELETE FROM EMPLOYEE WHERE EMPLOYEE_ID = ?",
                statement -> statement.setInt(1, id));
    }

    // 一括削除：部署の全社員を削除し、削除件数を返す
    public int deleteByDepartmentId(int departmentId) throws SQLException {
        return executeUpdate("DELETE FROM EMPLOYEE WHERE DEPARTMENT_ID = ?",
                statement -> statement.setInt(1, departmentId));
    }

    // 1操作を1トランザクションとして確定する（複数操作の一括確定は jdbc-transaction で扱う）
    private int executeUpdate(String sql, StatementBinder binder) throws SQLException {
        try (Connection connection = connectionProvider.open()) {
            connection.setAutoCommit(false);
            try {
                int count;
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    binder.bind(statement);
                    count = statement.executeUpdate();
                }
                connection.commit();
                return count;
            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    exception.addSuppressed(rollbackFailure);
                }
                throw exception;
            }
        }
    }

    // 社員の変換
    private static Employee map(ResultSet resultSet) throws SQLException {
        return new Employee(resultSet.getInt("EMPLOYEE_ID"),
                resultSet.getInt("DEPARTMENT_ID"),
                resultSet.getString("EMPLOYEE_NAME"),
                resultSet.getBigDecimal("SALARY"));
    }

    /*
     * statementbinderの契約を定義するインターフェース
     */
    @FunctionalInterface
    private interface StatementBinder {
        // bindの実行
        void bind(PreparedStatement statement) throws SQLException;
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
