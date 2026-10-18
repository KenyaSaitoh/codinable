package pro.kensait.jakartaee.employee.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import pro.kensait.jakartaee.employee.entity.Employee;
import pro.kensait.jakartaee.employee.entity.EmployeeSearchCriteria;

/** EMPLOYEE テーブルを JDBC で読み書きする（データアクセス層） */
@ApplicationScoped
public class EmployeeRepository {
    private static final String SELECT_COLUMNS = """
            SELECT EMPLOYEE_ID, EMPLOYEE_CODE, EMPLOYEE_NAME, DEPARTMENT_ID,
                   JOB_ID, SALARY, ENTRANCE_DATE, STATUS, VERSION
              FROM EMPLOYEE
            """;

    @Resource(lookup = "java:app/jdbc/EmployeeDS")
    private DataSource dataSource;

    /** WHERE 句と、? へ渡す値の並び */
    private record SearchSql(String whereClause, List<Object> parameters) {
    }

    public Optional<Employee> findById(Integer employeeId) {
        String sql = SELECT_COLUMNS + " WHERE EMPLOYEE_ID = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, employeeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(toEmployee(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new RepositoryException("社員の取得に失敗しました", exception);
        }
    }

    public List<Employee> search(
            EmployeeSearchCriteria criteria, int offset, int limit) {
        SearchSql searchSql = searchSql(criteria);
        String sql = SELECT_COLUMNS + searchSql.whereClause()
                + " ORDER BY EMPLOYEE_ID"
                + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<Object> parameters = new ArrayList<>(searchSql.parameters());
        parameters.add(offset);
        parameters.add(limit);
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Employee> employees = new ArrayList<>();
                while (resultSet.next()) {
                    employees.add(toEmployee(resultSet));
                }
                return employees;
            }
        } catch (SQLException exception) {
            throw new RepositoryException("社員の検索に失敗しました", exception);
        }
    }

    // search と同じ条件（searchSql）で件数を数える
    public long count(EmployeeSearchCriteria criteria) {
        SearchSql searchSql = searchSql(criteria);
        String sql = "SELECT COUNT(*) FROM EMPLOYEE" + searchSql.whereClause();
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, searchSql.parameters());
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        } catch (SQLException exception) {
            throw new RepositoryException("社員の件数の取得に失敗しました", exception);
        }
    }

    public Employee insert(Employee employee) {
        String sql = """
                INSERT INTO EMPLOYEE (
                  EMPLOYEE_CODE, EMPLOYEE_NAME, DEPARTMENT_ID,
                  JOB_ID, SALARY, ENTRANCE_DATE, STATUS, VERSION)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, employee.getEmployeeCode());
            statement.setString(2, employee.getEmployeeName());
            statement.setObject(3, employee.getDepartmentId(), Types.INTEGER);
            statement.setInt(4, employee.getJobId());
            statement.setInt(5, employee.getSalary());
            statement.setObject(6, employee.getEntranceDate());
            statement.setString(7, employee.getStatus());
            statement.setInt(8, employee.getVersion());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new RepositoryException("社員IDを取得できませんでした");
                }
                employee.setEmployeeId(keys.getInt(1));
            }
            return employee;
        } catch (SQLException exception) {
            throw new RepositoryException("社員の登録に失敗しました", exception);
        }
    }

    public int updateCode(Employee employee) {
        String sql = "UPDATE EMPLOYEE SET EMPLOYEE_CODE = ? WHERE EMPLOYEE_ID = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employee.getEmployeeCode());
            statement.setInt(2, employee.getEmployeeId());
            return statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RepositoryException("社員コードの更新に失敗しました", exception);
        }
    }

    public int update(Employee employee, int expectedVersion) {
        String sql = """
                UPDATE EMPLOYEE
                   SET EMPLOYEE_NAME = ?, DEPARTMENT_ID = ?, JOB_ID = ?,
                       SALARY = ?, ENTRANCE_DATE = ?, VERSION = ?
                 WHERE EMPLOYEE_ID = ? AND VERSION = ?
                """;
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employee.getEmployeeName());
            statement.setObject(2, employee.getDepartmentId(), Types.INTEGER);
            statement.setInt(3, employee.getJobId());
            statement.setInt(4, employee.getSalary());
            statement.setObject(5, employee.getEntranceDate());
            statement.setInt(6, employee.getVersion());
            statement.setInt(7, employee.getEmployeeId());
            statement.setInt(8, expectedVersion);
            return statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RepositoryException("社員の更新に失敗しました", exception);
        }
    }

    // 論理削除: 行は消さずに STATUS を変える
    public int updateStatus(Employee employee) {
        String sql = "UPDATE EMPLOYEE SET STATUS = ? WHERE EMPLOYEE_ID = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, employee.getStatus());
            statement.setInt(2, employee.getEmployeeId());
            return statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RepositoryException("社員の削除に失敗しました", exception);
        }
    }

    private SearchSql searchSql(EmployeeSearchCriteria criteria) {
        StringBuilder where = new StringBuilder(" WHERE STATUS = 'active'");
        List<Object> parameters = new ArrayList<>();
        append(where, parameters, criteria.getKeyword(),
                " AND EMPLOYEE_NAME LIKE ?", "%" + criteria.getKeyword() + "%");
        append(where, parameters, criteria.getDepartmentId(),
                " AND DEPARTMENT_ID = ?", criteria.getDepartmentId());
        append(where, parameters, criteria.getJobId(),
                " AND JOB_ID = ?", criteria.getJobId());
        append(where, parameters, criteria.getSalaryFrom(),
                " AND ? <= SALARY", criteria.getSalaryFrom());
        append(where, parameters, criteria.getSalaryTo(),
                " AND SALARY <= ?", criteria.getSalaryTo());
        return new SearchSql(where.toString(), parameters);
    }

    // 条件の値が null でないときだけ、SQL の断片と値を同じ順序で追加する
    private void append(StringBuilder where, List<Object> parameters,
            Object condition, String fragment, Object value) {
        if (condition != null) {
            where.append(fragment);
            parameters.add(value);
        }
    }

    private void bind(PreparedStatement statement, List<Object> parameters)
            throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            statement.setObject(i + 1, parameters.get(i));
        }
    }

    private Employee toEmployee(ResultSet resultSet) throws SQLException {
        Employee employee = new Employee();
        employee.setEmployeeId(resultSet.getInt("EMPLOYEE_ID"));
        employee.setEmployeeCode(resultSet.getString("EMPLOYEE_CODE"));
        employee.setEmployeeName(resultSet.getString("EMPLOYEE_NAME"));
        employee.setDepartmentId(
                resultSet.getObject("DEPARTMENT_ID", Integer.class));
        employee.setJobId(resultSet.getInt("JOB_ID"));
        employee.setSalary(resultSet.getInt("SALARY"));
        employee.setEntranceDate(
                resultSet.getObject("ENTRANCE_DATE", LocalDate.class));
        employee.setStatus(resultSet.getString("STATUS"));
        employee.setVersion(resultSet.getInt("VERSION"));
        return employee;
    }
}
