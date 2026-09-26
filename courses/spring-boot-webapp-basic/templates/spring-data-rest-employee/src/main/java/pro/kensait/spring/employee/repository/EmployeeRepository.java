package pro.kensait.spring.employee.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import pro.kensait.spring.employee.entity.Employee;
/** Controllerを書かず、CRUDと検索メソッドをHTTP APIとして公開する */
@RepositoryRestResource(path = "employees", collectionResourceRel = "employees")
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    // 第10章のquery_by_salaryに対応Pageableを付けると検索結果もページングできる
    @RestResource(path = "by-salary", rel = "by-salary")
    Page<Employee> findBySalaryGreaterThanEqual(
            @Param("lowerSalary") Integer lowerSalary, Pageable pageable);

    // 部署IDの検索
    @RestResource(path = "by-department", rel = "by-department")
    Page<Employee> findByDepartmentId(@Param("departmentId") Integer departmentId, Pageable pageable);

    // 役職IDの検索
    @RestResource(path = "by-job", rel = "by-job")
    Page<Employee> findByJobId(@Param("jobId") Integer jobId, Pageable pageable);

    // 社員名称containingの検索
    @RestResource(path = "by-name", rel = "by-name")
    Page<Employee> findByEmployeeNameContaining(@Param("keyword") String keyword, Pageable pageable);

    // 内部用のメソッドまで検索APIとして公開しない
    @RestResource(exported = false)
    List<Employee> findByStatus(String status);
}
