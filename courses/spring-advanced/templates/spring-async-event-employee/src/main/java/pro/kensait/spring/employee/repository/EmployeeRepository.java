package pro.kensait.spring.employee.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pro.kensait.spring.employee.entity.Employee;

/*
 * 社員のリポジトリ
 */
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    List<Employee> findByDepartmentId(Integer departmentId);
}
