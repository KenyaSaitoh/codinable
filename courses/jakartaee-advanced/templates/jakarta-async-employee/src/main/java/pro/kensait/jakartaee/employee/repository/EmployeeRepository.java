package pro.kensait.jakartaee.employee.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import pro.kensait.jakartaee.employee.entity.Employee;

/*
 * 社員の読み書き（Jakarta Persistence）
 * トランザクションは呼び出し側（EmployeeService の @Transactional など）が決める
 */
@ApplicationScoped
public class EmployeeRepository {
    @PersistenceContext(unitName = "employeePU")
    private EntityManager em;

    public Optional<Employee> findById(Integer employeeId) {
        return Optional.ofNullable(em.find(Employee.class, employeeId));
    }

    public List<Employee> findByDepartmentId(Integer departmentId) {
        return em.createQuery("SELECT e FROM Employee e WHERE e.departmentId = :id ORDER BY e.employeeId",
                        Employee.class)
                .setParameter("id", departmentId)
                .getResultList();
    }

    public List<Employee> findAllOrderById() {
        return em.createQuery("SELECT e FROM Employee e ORDER BY e.employeeId", Employee.class)
                .getResultList();
    }
}
