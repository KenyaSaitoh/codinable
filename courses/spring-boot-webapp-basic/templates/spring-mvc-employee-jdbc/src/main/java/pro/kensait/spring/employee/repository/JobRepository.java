package pro.kensait.spring.employee.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import pro.kensait.spring.employee.entity.Job;
/** JDBC Templateによる役職マスタアクセス */
@Repository
public class JobRepository {
    private final JdbcTemplate jdbcTemplate;

    // 役職の初期化
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "Spring manages the injected collaborator")
    public JobRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 全件検索
    public List<Job> findAll() {
        return jdbcTemplate.query("""
                SELECT JOB_ID, JOB_NAME, GRADE FROM JOB ORDER BY GRADE
                """, (resultSet, rowNumber) -> new Job(
                resultSet.getInt("JOB_ID"), resultSet.getString("JOB_NAME"),
                resultSet.getInt("GRADE")));
    }

    // リポジトリメソッド：主キー検索によって社員の取得
    public Optional<Job> findById(Integer jobId) {
        return jdbcTemplate.query("""
                SELECT JOB_ID, JOB_NAME, GRADE FROM JOB WHERE JOB_ID = ?
                """, (resultSet, rowNumber) -> new Job(
                resultSet.getInt("JOB_ID"), resultSet.getString("JOB_NAME"),
                resultSet.getInt("GRADE")), jobId).stream().findFirst();
    }
}
