package pro.kensait.spring.employee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import pro.kensait.spring.employee.entity.Department;
/** JDBC Templateによる部署マスタアクセス */
@Repository
public class DepartmentRepository {
    private final JdbcTemplate jdbcTemplate;

    // 部署の初期化
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "Spring manages the injected collaborator")
    public DepartmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 全件検索
    public List<Department> findAll() {
        return jdbcTemplate.query("""
                SELECT DEPARTMENT_ID, DEPARTMENT_NAME, LOCATION
                  FROM DEPARTMENT ORDER BY DEPARTMENT_ID
                """, (resultSet, rowNumber) -> new Department(
                resultSet.getInt("DEPARTMENT_ID"),
                resultSet.getString("DEPARTMENT_NAME"),
                resultSet.getString("LOCATION")));
    }

    // リポジトリメソッド：主キー検索によって社員の取得
    public Optional<Department> findById(Integer departmentId) {
        return jdbcTemplate.query("""
                SELECT DEPARTMENT_ID, DEPARTMENT_NAME, LOCATION
                  FROM DEPARTMENT WHERE DEPARTMENT_ID = ?
                """, (resultSet, rowNumber) -> new Department(
                resultSet.getInt("DEPARTMENT_ID"),
                resultSet.getString("DEPARTMENT_NAME"),
                resultSet.getString("LOCATION")), departmentId).stream().findFirst();
    }
}
