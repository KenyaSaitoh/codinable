package pro.kensait.spring.employee.graphql.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pro.kensait.spring.employee.graphql.entity.Employee;

/*
 * 社員テーブルにアクセスするためのリポジトリインタフェース
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    // 部署ID一覧の検索
    @Query("SELECT e FROM Employee e JOIN FETCH e.department WHERE e.department.departmentId IN :ids")
    List<Employee> findByDepartmentIds(@Param("ids") List<Integer> ids);

    // 部署IDをキーに社員の検索
    @Query("SELECT e FROM Employee e WHERE e.department.departmentId = :departmentId")
    List<Employee> findByDepartmentId(@Param("departmentId") Integer departmentId);

    // 給与範囲（下限と上限）をキーに社員の検索
    @Query("SELECT e FROM Employee e "
            + "WHERE :lowerSalary <= e.salary AND e.salary <= :upperSalary")
    List<Employee> findBySalaryRange(@Param("lowerSalary") Integer lowerSalary,
            @Param("upperSalary") Integer upperSalary);
}
