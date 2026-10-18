package pro.kensait.jakartaee.employee.repository;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import pro.kensait.jakartaee.employee.entity.Department;
import pro.kensait.jakartaee.employee.entity.Employee;
import pro.kensait.jakartaee.employee.entity.EmployeeSearchCriteria;
import pro.kensait.jakartaee.employee.entity.Job;

/** 社員と部署・役職を EntityManager で読み書きする（データアクセス層） */
@ApplicationScoped
public class EmployeeRepository {
    // 検索と件数取得で共通の条件。null のパラメータはその条件を使わない
    // （演習用: EclipseLink は null のパラメータを SQL に NULL と書き込み、HSQLDB は LIKE の右辺の NULL をエラーにするので、
    //   キーワードだけ COALESCE で空文字列にしている。:keyword IS NULL の判定は変わらない）
    private static final String CONDITIONS = """
             WHERE e.status = 'active'
               AND (:keyword IS NULL
                    OR e.employeeName LIKE CONCAT('%', COALESCE(:keyword, ''), '%'))
               AND (:departmentId IS NULL
                    OR d.departmentId = :departmentId)
               AND (:jobId IS NULL OR e.job.jobId = :jobId)
               AND (:salaryFrom IS NULL OR :salaryFrom <= e.salary)
               AND (:salaryTo IS NULL OR e.salary <= :salaryTo)
            """;

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

    // 月給が下限以上の在籍中の社員（社員ID順）
    public List<Employee> findByLowerSalary(int lowerSalary) {
        return entityManager.createQuery(
                "SELECT e FROM Employee e WHERE e.status = 'active'"
                + " AND :lowerSalary <= e.salary ORDER BY e.employeeId",
                Employee.class)
                .setParameter("lowerSalary", lowerSalary)
                .getResultList();
    }

    public List<Employee> search(EmployeeSearchCriteria criteria,
            int offset, int limit) {
        TypedQuery<Employee> query = entityManager.createQuery(
                "SELECT e FROM Employee e LEFT JOIN e.department d"
                + CONDITIONS + " ORDER BY e.employeeId",
                Employee.class);
        bind(query, criteria);
        return query.setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public long count(EmployeeSearchCriteria criteria) {
        TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(e) FROM Employee e LEFT JOIN e.department d"
                + CONDITIONS, Long.class);
        bind(query, criteria);
        return query.getSingleResult();
    }

    private void bind(TypedQuery<?> query,
            EmployeeSearchCriteria criteria) {
        query.setParameter("keyword", criteria.getKeyword());
        query.setParameter("departmentId", criteria.getDepartmentId());
        query.setParameter("jobId", criteria.getJobId());
        query.setParameter("salaryFrom", criteria.getSalaryFrom());
        query.setParameter("salaryTo", criteria.getSalaryTo());
    }
}
