package pro.kensait.jakartaee.employee.graphql.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import pro.kensait.jakartaee.employee.graphql.api.EmployeeInput;
import pro.kensait.jakartaee.employee.graphql.entity.Department;
import pro.kensait.jakartaee.employee.graphql.entity.Employee;

/*
 * 社員の業務処理とデータアクセス
 * Jakarta Transactions の既定では、チェック例外ではロールバックしない
 * （このクラスはデータを変更する前に例外を送出するので問題にならない）
 */
@ApplicationScoped
@Transactional
public class EmployeeService {
    private static final Logger logger = Logger.getLogger(EmployeeService.class.getName());

    @PersistenceContext(unitName = "employeePU")
    private EntityManager em;
    @Inject
    private Validator validator;

    public Employee getEmployee(int id) throws EmployeeNotFoundException {
        Employee employee = em.find(Employee.class, id);
        if (employee == null) {
            throw new EmployeeNotFoundException("社員が存在しません => " + id);
        }
        return employee;
    }

    public List<Employee> getEmployeesAll() {
        return em.createQuery("SELECT e FROM Employee e ORDER BY e.employeeId", Employee.class)
                .getResultList();
    }

    // 部署ごとに呼ばれる版（N+1 問題の説明用。EmployeeGraphQLApi のコメントを参照）
    public List<Employee> getEmployeesByDepartment(int departmentId) {
        logger.info("[ EmployeeService#getEmployeesByDepartment ] departmentId=" + departmentId);
        return em.createQuery("SELECT e FROM Employee e WHERE e.department.departmentId = :id"
                        + " ORDER BY e.employeeId", Employee.class)
                .setParameter("id", departmentId)
                .getResultList();
    }

    // 部署をまとめて受け取り、1 回の JPQL で社員を検索して部署ごとに振り分ける
    public List<List<Employee>> employeesByDepartments(List<Department> departments) {
        logger.info("[ EmployeeService#employeesByDepartments ] departments=" + departments.size());
        List<Integer> ids = departments.stream().map(Department::getDepartmentId).toList();
        List<Employee> employees = em.createQuery(
                "SELECT e FROM Employee e JOIN FETCH e.department"
                        + " WHERE e.department.departmentId IN :ids", Employee.class)
                .setParameter("ids", ids)
                .getResultList(); // 【1】
        Map<Integer, List<Employee>> byDepartment = employees.stream()
                .collect(Collectors.groupingBy(e -> e.getDepartment().getDepartmentId())); // 【2】
        return departments.stream()
                .map(d -> byDepartment.getOrDefault(d.getDepartmentId(), List.of())) // 【3】
                .toList();
    }

    public Employee createEmployee(EmployeeInput input) throws InvalidEmployeeException {
        if (!validator.validate(input).isEmpty()) { // 【1】
            throw new InvalidEmployeeException("社員名・部署・給与・日付を確認してください");
        }
        Department department = em.find(Department.class, input.departmentId()); // 【2】
        if (department == null) {
            throw new InvalidEmployeeException("部署が存在しません => " + input.departmentId());
        }
        Employee employee = new Employee(input.employeeName(), department,
                input.jobName(), input.salary(), input.entranceDate());
        em.persist(employee); // 【3】
        em.flush();
        return employee; // 【4】
    }

    public Employee updateEmployee(int id, EmployeeInput input)
            throws EmployeeNotFoundException, InvalidEmployeeException {
        if (!validator.validate(input).isEmpty()) {
            throw new InvalidEmployeeException("社員名・部署・給与・日付を確認してください");
        }
        Employee employee = getEmployee(id);
        Department department = em.find(Department.class, input.departmentId());
        if (department == null) {
            throw new InvalidEmployeeException("部署が存在しません => " + input.departmentId());
        }
        employee.setEmployeeName(input.employeeName());
        employee.setDepartment(department);
        employee.setJobName(input.jobName());
        employee.setSalary(input.salary());
        employee.setEntranceDate(input.entranceDate());
        return employee;
    }

    public int deleteEmployee(int id) throws EmployeeNotFoundException {
        em.remove(getEmployee(id));
        return id;
    }
}
