package pro.kensait.jakartaee.employee.service;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import pro.kensait.jakartaee.employee.entity.Department;
import pro.kensait.jakartaee.employee.entity.Employee;
import pro.kensait.jakartaee.employee.entity.EmployeeSearchCriteria;
import pro.kensait.jakartaee.employee.entity.Job;
import pro.kensait.jakartaee.employee.repository.DepartmentRepository;
import pro.kensait.jakartaee.employee.repository.EmployeeRepository;
import pro.kensait.jakartaee.employee.repository.JobRepository;

/** 社員管理の業務処理（ビジネス層）。検索範囲・ページング・社員コードの採番・競合の判定・トランザクションの境界 */
@ApplicationScoped
public class EmployeeService {
    public static final int PAGE_SIZE = 5;

    @Inject
    private EmployeeRepository employeeRepository;
    @Inject
    private DepartmentRepository departmentRepository;
    @Inject
    private JobRepository jobRepository;

    public List<Department> departments() {
        return departmentRepository.findAll();
    }

    public List<Job> jobs() {
        return jobRepository.findAll();
    }

    public EmployeePage search(EmployeeSearchCriteria criteria, int page) {
        if (criteria.rangeReversed()) {
            throw new RangeException();
        }
        int current = Math.max(page, 1);
        long total = employeeRepository.count(criteria);
        int totalPages = (int) ((total + PAGE_SIZE - 1) / PAGE_SIZE);
        List<Employee> employees = employeeRepository.search(criteria,
                (current - 1) * PAGE_SIZE, PAGE_SIZE);
        return new EmployeePage(employees, totalPages, total);
    }

    // 在籍中（status が active）の社員を取得する。見つからなければ EmployeeNotFoundException
    public Employee get(Integer employeeId) {
        return employeeRepository.findById(employeeId)
                .filter(employee -> Employee.ACTIVE.equals(employee.getStatus()))
                .orElseThrow(EmployeeNotFoundException::new);
    }

    @Transactional
    public Employee create(Employee employee) {
        employee.setStatus(Employee.ACTIVE);
        employee.setVersion(0);
        employee.setEmployeeCode("E0000");
        Employee saved = employeeRepository.insert(employee);
        saved.setEmployeeCode(String.format("E%04d", saved.getEmployeeId()));
        employeeRepository.updateCode(saved);
        return saved;
    }

    // 画面に表示したときの version のままなら更新する。更新件数が 0 なら他の人が先に更新している
    @Transactional
    public Employee update(Integer employeeId, Employee input, Integer version) {
        Employee current = get(employeeId);
        if (version == null || !version.equals(current.getVersion())) {
            throw new ConflictException();
        }
        current.setEmployeeName(input.getEmployeeName());
        current.setDepartmentId(input.getDepartmentId());
        current.setJobId(input.getJobId());
        current.setSalary(input.getSalary());
        current.setEntranceDate(input.getEntranceDate());
        current.setVersion(version + 1);
        if (employeeRepository.update(current, version) == 0) {
            throw new ConflictException();
        }
        return current;
    }

    // 論理削除（状態を deleted に変える）
    @Transactional
    public void delete(Integer employeeId) {
        Employee current = get(employeeId);
        current.setStatus(Employee.DELETED);
        employeeRepository.updateStatus(current);
    }

    // 部署なし（DEPARTMENT_ID が NULL）の社員もいるので、そのときは空欄にする
    public String departmentNameOf(Integer departmentId) {
        if (departmentId == null) {
            return "";
        }
        return departmentRepository.findById(departmentId)
                .map(Department::departmentName).orElse("");
    }

    public String jobNameOf(Integer jobId) {
        return jobRepository.findById(jobId).map(Job::jobName).orElse("");
    }
}
