package pro.kensait.db.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    // 社員の登録（社員番号はDBの自動採番に任せ、採番された社員番号を返す）
    public int insertAndReturnId(Employee employee) throws SQLException {
        String sql = "INSERT INTO EMPLOYEE "
                + "(DEPARTMENT_ID, EMPLOYEE_NAME, SALARY) VALUES (?, ?, ?)";
        try (Connection connection = connectionProvider.open()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, employee.departmentId());
                statement.setString(2, employee.name());
                statement.setBigDecimal(3, employee.salary());
                statement.executeUpdate();
                // 採番されたキーをResultSetとして受け取る
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    generatedKeys.next();
                    int generatedId = generatedKeys.getInt(1);
                    connection.commit();
                    return generatedId;
                }
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
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

    // 楽観ロック：読み取り時のバージョン番号（expectedVersion）と一致する場合だけ給与を更新する
    // 他者が先に更新してVERSIONが進んでいれば、更新件数0が返る
    public int updateSalaryWithVersion(int employeeId, BigDecimal newSalary, int expectedVersion)
            throws SQLException {
        String sql = "UPDATE EMPLOYEE SET SALARY = ?, VERSION = VERSION + 1 "
                + "WHERE EMPLOYEE_ID = ? AND VERSION = ?";
        return executeUpdate(sql, statement -> {
            statement.setBigDecimal(1, newSalary);
            statement.setInt(2, employeeId);
            statement.setInt(3, expectedVersion);
        });
    }

    // 楽観ロック：社員の現在のバージョン番号を取得する
    public int findVersionById(int employeeId) throws SQLException {
        String sql = "SELECT VERSION FROM EMPLOYEE WHERE EMPLOYEE_ID = ?";
        try (Connection connection = connectionProvider.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, employeeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Employee not found: " + employeeId);
                }
                return resultSet.getInt("VERSION");
            }
        }
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
