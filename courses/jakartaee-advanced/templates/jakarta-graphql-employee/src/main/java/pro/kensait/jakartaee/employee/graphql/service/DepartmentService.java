package pro.kensait.jakartaee.employee.graphql.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import pro.kensait.jakartaee.employee.graphql.entity.Department;

/*
 * 部署の検索
 */
@ApplicationScoped
@Transactional
public class DepartmentService {
    @PersistenceContext(unitName = "employeePU")
    private EntityManager em;

    public List<Department> getDepartmentsAll() {
        return em.createQuery("SELECT d FROM Department d ORDER BY d.departmentId", Department.class)
                .getResultList();
    }
}
