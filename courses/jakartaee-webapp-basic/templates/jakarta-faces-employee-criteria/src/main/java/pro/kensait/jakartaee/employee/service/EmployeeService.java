package pro.kensait.jakartaee.employee.service;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import pro.kensait.jakartaee.employee.entity.Department;
import pro.kensait.jakartaee.employee.entity.Employee;
import pro.kensait.jakartaee.employee.entity.EmployeeSearchCriteria;
import pro.kensait.jakartaee.employee.entity.Job;
import pro.kensait.jakartaee.employee.repository.EmployeeRepository;

/** 社員管理の業務処理（ビジネス層）。検索範囲・ページング・社員コードの採番・競合の判定・トランザクションの境界 */
@ApplicationScoped
public class EmployeeService {
    public static final int PAGE_SIZE = 5;

    @Inject
    private EmployeeRepository employeeRepository;

    public List<Department> departments() {
        return employeeRepository.findDepartments();
    }

    public List<Job> jobs() {
        return employeeRepository.findJobs();
    }

    public Department department(Integer departmentId) {
        return employeeRepository.findDepartment(departmentId);
    }

    public Job job(Integer jobId) {
        return employeeRepository.findJob(jobId);
    }

    // @Transactional が無いので、返した社員は呼び出し元に届いた時点で detached になる
    public EmployeePage search(EmployeeSearchCriteria criteria, int page) {
        if (criteria.rangeReversed()) {
            throw new RangeException();
        }
        int current = Math.max(page, 1);
        long total = employeeRepository.count(criteria);
        int totalPages = (int) ((total + PAGE_SIZE - 1) / PAGE_SIZE);
        List<Employee> employees = employeeRepository.search(
                criteria, (current - 1) * PAGE_SIZE, PAGE_SIZE);
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
        employee.setEmployeeCode("E0000");
        employeeRepository.persist(employee);
        employeeRepository.flush();
        employee.setEmployeeCode(
                String.format("E%04d", employee.getEmployeeId()));
        return employee;
    }

    @Transactional
    public Employee update(Integer employeeId, Employee input,
            Integer version) {
        Employee current = get(employeeId);
        if (version == null || !version.equals(current.getVersion())) {
            throw new ConflictException();
        }
        current.setEmployeeName(input.getEmployeeName());
        current.setDepartment(input.getDepartment());
        current.setJob(input.getJob());
        current.setSalary(input.getSalary());
        current.setEntranceDate(input.getEntranceDate());
        try {
            employeeRepository.flush();
        } catch (OptimisticLockException e) {
            throw new ConflictException();
        }
        return current;
    }

    // 論理削除。managed のエンティティの状態を変えるだけで、コミット時に UPDATE が発行される
    @Transactional
    public void delete(Integer employeeId) {
        Employee current = get(employeeId);
        current.setStatus(Employee.DELETED);
    }
}
