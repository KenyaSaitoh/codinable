package pro.kensait.spring.employee.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import pro.kensait.spring.employee.entity.Employee;
import pro.kensait.spring.employee.entity.EmployeeSearchCriteria;
/** JDBC Templateによる社員アクセス */
@Repository
@SuppressFBWarnings(value = "OBL_UNSATISFIED_OBLIGATION_EXCEPTION_EDGE",
        justification = "JdbcTemplate closes prepared statements created by its callback")
public class EmployeeRepository {
    private static final String SELECT_COLUMNS = """
            SELECT EMPLOYEE_ID, EMPLOYEE_CODE, EMPLOYEE_NAME, DEPARTMENT_ID,
                   JOB_ID, SALARY, ENTRANCE_DATE, STATUS, VERSION
              FROM EMPLOYEE
            """;

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Employee> rowMapper = (resultSet, rowNumber) -> {
        Employee employee = new Employee();
        employee.setEmployeeId(resultSet.getInt("EMPLOYEE_ID"));
        employee.setEmployeeCode(resultSet.getString("EMPLOYEE_CODE"));
        employee.setEmployeeName(resultSet.getString("EMPLOYEE_NAME"));
        employee.setDepartmentId(resultSet.getInt("DEPARTMENT_ID"));
        employee.setJobId(resultSet.getInt("JOB_ID"));
        employee.setSalary(resultSet.getInt("SALARY"));
        employee.setEntranceDate(resultSet.getDate("ENTRANCE_DATE").toLocalDate());
        employee.setStatus(resultSet.getString("STATUS"));
        employee.setVersion(resultSet.getInt("VERSION"));
        return employee;
    };

    // 社員の初期化
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "Spring manages the injected collaborator")
    public EmployeeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // データの検索
    public List<Employee> search(EmployeeSearchCriteria criteria, int offset, int limit) {
        SearchSql searchSql = searchSql(criteria);
        String sql = SELECT_COLUMNS + searchSql.whereClause()
                + " ORDER BY EMPLOYEE_ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<Object> parameters = new ArrayList<>(searchSql.parameters());
        parameters.add(offset);
        parameters.add(limit);
        return jdbcTemplate.query(sql, rowMapper, parameters.toArray());
    }

    // 件数の実行
    public long count(EmployeeSearchCriteria criteria) {
        SearchSql searchSql = searchSql(criteria);
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM EMPLOYEE "
                + searchSql.whereClause(), Long.class, searchSql.parameters().toArray());
        return count == null ? 0 : count;
    }

    // リポジトリメソッド：主キー検索によって社員の取得
    public Optional<Employee> findById(Integer employeeId) {
        return jdbcTemplate.query(SELECT_COLUMNS + " WHERE EMPLOYEE_ID = ?",
                rowMapper, employeeId).stream().findFirst();
    }

    // 社員の登録
    public Employee insert(Employee employee) {
        String sql = """
                INSERT INTO EMPLOYEE (EMPLOYEE_CODE, EMPLOYEE_NAME, DEPARTMENT_ID,
                  JOB_ID, SALARY, ENTRANCE_DATE, STATUS, VERSION)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, employee.getEmployeeCode());
            statement.setString(2, employee.getEmployeeName());
            statement.setInt(3, employee.getDepartmentId());
            statement.setInt(4, employee.getJobId());
            statement.setInt(5, employee.getSalary());
            statement.setDate(6, Date.valueOf(employee.getEntranceDate()));
            statement.setString(7, employee.getStatus());
            statement.setInt(8, employee.getVersion());
            return statement;
        }, keyHolder);
        Number generatedKey = keyHolder.getKey();
        if (generatedKey == null) {
            throw new IllegalStateException("社員IDを取得できませんでした");
        }
        employee.setEmployeeId(generatedKey.intValue());
        return employee;
    }

    // コードの更新
    public void updateCode(Employee employee) {
        jdbcTemplate.update("UPDATE EMPLOYEE SET EMPLOYEE_CODE = ? WHERE EMPLOYEE_ID = ?",
                employee.getEmployeeCode(), employee.getEmployeeId());
    }

    // 一件更新
    public void update(Employee employee) {
        jdbcTemplate.update("""
                UPDATE EMPLOYEE
                   SET EMPLOYEE_NAME = ?, DEPARTMENT_ID = ?, JOB_ID = ?, SALARY = ?,
                       ENTRANCE_DATE = ?, VERSION = ?
                 WHERE EMPLOYEE_ID = ?
                """, employee.getEmployeeName(), employee.getDepartmentId(),
                employee.getJobId(), employee.getSalary(),
                Date.valueOf(employee.getEntranceDate()), employee.getVersion(),
                employee.getEmployeeId());
    }

    // 状態の更新
    public void updateStatus(Employee employee) {
        jdbcTemplate.update("UPDATE EMPLOYEE SET STATUS = ? WHERE EMPLOYEE_ID = ?",
                employee.getStatus(), employee.getEmployeeId());
    }

    // SQLの検索
    private SearchSql searchSql(EmployeeSearchCriteria criteria) {
        StringBuilder where = new StringBuilder(" WHERE STATUS = 'active'");
        List<Object> parameters = new ArrayList<>();
        append(where, parameters, criteria.getKeyword(), " AND EMPLOYEE_NAME LIKE ?",
                "%" + criteria.getKeyword() + "%");
        append(where, parameters, criteria.getDepartmentId(),
                " AND DEPARTMENT_ID = ?", criteria.getDepartmentId());
        append(where, parameters, criteria.getJobId(),
                " AND JOB_ID = ?", criteria.getJobId());
        append(where, parameters, criteria.getSalaryFrom(),
                " AND SALARY >= ?", criteria.getSalaryFrom());
        append(where, parameters, criteria.getSalaryTo(),
                " AND SALARY <= ?", criteria.getSalaryTo());
        return new SearchSql(where.toString(), parameters);
    }

    // appendの実行
    private void append(StringBuilder sql, List<Object> parameters, Object condition,
            String clause, Object value) {
        if (condition != null) {
            sql.append(clause);
            parameters.add(value);
        }
    }

    /*
     * 検索SQLを表すレコード
     */
    private record SearchSql(String whereClause, List<Object> parameters) {
    }
}
