package pro.kensait.spring.employee.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pro.kensait.spring.employee.entity.Employee;
import pro.kensait.spring.employee.entity.EmployeeSearchCriteria;

/*
 * 社員情報を永続化するリポジトリ
 */
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    // アクションメソッド：書籍を検索する（静的なクエリを使用する）
    @Query("""
            SELECT e FROM Employee e
             WHERE e.status = 'active'
               AND (:#{#criteria.keyword} IS NULL
                    OR e.employeeName LIKE CONCAT('%', :#{#criteria.keyword}, '%'))
               AND (:#{#criteria.departmentId} IS NULL
                    OR e.departmentId = :#{#criteria.departmentId})
               AND (:#{#criteria.jobId} IS NULL OR e.jobId = :#{#criteria.jobId})
               AND (:#{#criteria.salaryFrom} IS NULL OR e.salary >= :#{#criteria.salaryFrom})
               AND (:#{#criteria.salaryTo} IS NULL OR e.salary <= :#{#criteria.salaryTo})
            """)
    Page<Employee> search(@Param("criteria") EmployeeSearchCriteria criteria,
            Pageable pageable);

    // 状態月給以上thanequal注文社員IDの検索
    List<Employee> findByStatusAndSalaryGreaterThanEqualOrderByEmployeeIdAsc(
            String status, Integer salary);

    // versionの照合と更新を1文で行い、同時更新でも後勝ちの上書きの防止
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Employee e SET e.employeeName = :#{#input.employeeName},
                   e.departmentId = :#{#input.departmentId}, e.jobId = :#{#input.jobId},
                   e.salary = :#{#input.salary}, e.entranceDate = :#{#input.entranceDate},
                   e.version = e.version + 1
             WHERE e.employeeId = :employeeId AND e.version = :version AND e.status = 'active'
            """)
    int updateIfVersionMatches(@Param("employeeId") Integer employeeId,
            @Param("input") Employee input, @Param("version") Integer version);

    // mark削除済みの実行
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Employee e SET e.status = 'deleted', e.version = e.version + 1
             WHERE e.employeeId = :employeeId AND e.status = 'active'
            """)
    int markDeleted(@Param("employeeId") Integer employeeId);
}
