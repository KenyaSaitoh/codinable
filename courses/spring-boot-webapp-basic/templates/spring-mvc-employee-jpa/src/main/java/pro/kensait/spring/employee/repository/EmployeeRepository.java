package pro.kensait.spring.employee.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pro.kensait.spring.employee.entity.Employee;
import pro.kensait.spring.employee.entity.EmployeeSearchCriteria;

/*
 * 社員テーブルにアクセスするためのリポジトリインタフェース
 */
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    // アクションメソッド：社員を検索する（静的なクエリを使用する）
    // 部署は LEFT JOIN で結合する（部署なし（DEPARTMENT_ID が NULL）の社員を落とさないため）
    @Query("""
            SELECT e FROM Employee e LEFT JOIN e.department d
             WHERE e.status = 'active'
               AND (:#{#criteria.keyword} IS NULL
                    OR e.employeeName LIKE CONCAT('%', :#{#criteria.keyword}, '%'))
               AND (:#{#criteria.departmentId} IS NULL
                    OR d.departmentId = :#{#criteria.departmentId})
               AND (:#{#criteria.jobId} IS NULL OR e.job.jobId = :#{#criteria.jobId})
               AND (:#{#criteria.salaryFrom} IS NULL OR e.salary >= :#{#criteria.salaryFrom})
               AND (:#{#criteria.salaryTo} IS NULL OR e.salary <= :#{#criteria.salaryTo})
            """)
    Page<Employee> search(@Param("criteria") EmployeeSearchCriteria criteria,
            Pageable pageable);
}
