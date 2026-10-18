package pro.kensait.jakartaee.employee.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import pro.kensait.jakartaee.employee.entity.Department;
import pro.kensait.jakartaee.employee.entity.Department_;
import pro.kensait.jakartaee.employee.entity.Employee;
import pro.kensait.jakartaee.employee.entity.EmployeeSearchCriteria;
import pro.kensait.jakartaee.employee.entity.Employee_;
import pro.kensait.jakartaee.employee.entity.Job;
import pro.kensait.jakartaee.employee.entity.Job_;

/**
 * 社員と部署・役職を EntityManager で読み書きする（データアクセス層）
 * 検索は、指定された条件だけを Criteria API で組み立てる。属性は静的メタモデル（Employee_ など）で参照する
 * （Employee_・Department_・Job_ はビルドのときにアノテーションプロセッサが生成する）
 */
@ApplicationScoped
public class EmployeeRepository {
    @PersistenceContext(unitName = "EmployeePU")
    private EntityManager entityManager;

    public Optional<Employee> findById(Integer employeeId) {
        return Optional.ofNullable(
                entityManager.find(Employee.class, employeeId));
    }

    public void persist(Employee employee) {
        entityManager.persist(employee);
    }

    public void flush() {
        entityManager.flush();
    }

    public List<Department> findDepartments() {
        return entityManager.createQuery(
                "SELECT d FROM Department d ORDER BY d.departmentId",
                Department.class).getResultList();
    }

    public List<Job> findJobs() {
        return entityManager.createQuery(
                "SELECT j FROM Job j ORDER BY j.jobId",
                Job.class).getResultList();
    }

    public Department findDepartment(Integer departmentId) {
        return departmentId == null ? null : entityManager.find(Department.class, departmentId);
    }

    public Job findJob(Integer jobId) {
        return jobId == null ? null : entityManager.find(Job.class, jobId);
    }

    public List<Employee> search(EmployeeSearchCriteria criteria,
            int offset, int limit) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Employee> query = cb.createQuery(Employee.class);
        Root<Employee> employee = query.from(Employee.class);
        query.select(employee)
                .where(predicates(cb, employee, criteria))
                .orderBy(cb.asc(employee.get(Employee_.employeeId)));
        return entityManager.createQuery(query)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public long count(EmployeeSearchCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Employee> employee = query.from(Employee.class);
        query.select(cb.count(employee))
                .where(predicates(cb, employee, criteria));
        return entityManager.createQuery(query).getSingleResult();
    }

    // 一覧用と件数用で同じ条件を作る。値が null の条件は加えない
    private List<Predicate> predicates(CriteriaBuilder cb,
            Root<Employee> employee, EmployeeSearchCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(
                employee.get(Employee_.status), Employee.ACTIVE));
        if (criteria.getKeyword() != null) {
            predicates.add(cb.like(employee.get(Employee_.employeeName),
                    "%" + criteria.getKeyword() + "%"));
        }
        if (criteria.getDepartmentId() != null) {
            predicates.add(cb.equal(employee.get(Employee_.department)
                    .get(Department_.departmentId),
                    criteria.getDepartmentId()));
        }
        if (criteria.getJobId() != null) {
            predicates.add(cb.equal(employee.get(Employee_.job)
                    .get(Job_.jobId), criteria.getJobId()));
        }
        if (criteria.getSalaryFrom() != null) {
            predicates.add(cb.ge(employee.get(Employee_.salary),
                    criteria.getSalaryFrom()));
        }
        if (criteria.getSalaryTo() != null) {
            predicates.add(cb.le(employee.get(Employee_.salary),
                    criteria.getSalaryTo()));
        }
        return predicates;
    }
}
