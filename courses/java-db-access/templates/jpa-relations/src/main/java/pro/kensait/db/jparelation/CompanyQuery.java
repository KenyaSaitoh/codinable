package pro.kensait.db.jparelation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.List;

/*
 * companyクエリの機能を提供するクラス
 */
public final class CompanyQuery {
    // 部署社員の検索
    public List<Department> findDepartmentsWithEmployees(EntityManager entityManager) {
        return entityManager.createQuery(
                "select distinct d from Department d left join fetch d.employees order by d.id",
                Department.class).getResultList();
    }

    // 社員月給最小の検索
    public List<Employee> findEmployeesWithSalaryAtLeast(EntityManager entityManager,
            BigDecimal minimum, int offset, int limit) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Employee> criteria = builder.createQuery(Employee.class);
        Root<Employee> employee = criteria.from(Employee.class);
        criteria.select(employee)
                .where(builder.greaterThanOrEqualTo(employee.get("salary"), minimum))
                .orderBy(builder.asc(employee.get("id")));
        return entityManager.createQuery(criteria)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
