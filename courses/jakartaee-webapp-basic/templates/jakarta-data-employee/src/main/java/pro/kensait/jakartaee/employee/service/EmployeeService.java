package pro.kensait.jakartaee.employee.service;

import java.util.Comparator;
import java.util.List;

import jakarta.data.exceptions.OptimisticLockingFailureException;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import pro.kensait.jakartaee.employee.entity.Department;
import pro.kensait.jakartaee.employee.entity.Employee;
import pro.kensait.jakartaee.employee.entity.Job;
import pro.kensait.jakartaee.employee.repository.DepartmentRepository;
import pro.kensait.jakartaee.employee.repository.EmployeeRepository;
import pro.kensait.jakartaee.employee.repository.JobRepository;

/**
 * 社員管理の業務処理（ビジネス層）
 * Jakarta Data の Repository は状態を持たないので、変更は insert・update の呼び出しで明示する
 */
@ApplicationScoped
public class EmployeeService {
    public static final int PAGE_SIZE = 5;

    @Inject
    private EmployeeRepository employeeRepository;
    @Inject
    private DepartmentRepository departmentRepository;
    @Inject
    private JobRepository jobRepository;

    // 演習用: create で flush するためだけに使う（Repository と同じ永続化ユニット）
    @PersistenceContext(unitName = "EmployeePU")
    private EntityManager entityManager;

    public List<Department> departments() {
        return departmentRepository.findAll()
                .sorted(Comparator.comparing(Department::getDepartmentId)).toList();
    }

    public List<Job> jobs() {
        return jobRepository.findAll()
                .sorted(Comparator.comparing(Job::getJobId)).toList();
    }

    public Department department(Integer departmentId) {
        return departmentId == null ? null : departmentRepository.findById(departmentId).orElse(null);
    }

    public Job job(Integer jobId) {
        return jobId == null ? null : jobRepository.findById(jobId).orElse(null);
    }

    // 在籍中の社員の人数（メソッド名による問い合わせ countByStatus）
    public long activeCount() {
        return employeeRepository.countByStatus(Employee.ACTIVE);
    }

    // 指定した役職の在籍中の社員（メソッド名による問い合わせ）
    public List<Employee> findByJob(Integer jobId) {
        return employeeRepository.findByStatusAndJobJobIdOrderByEmployeeId(
                Employee.ACTIVE, jobId);
    }

    // 社員コードで在籍中の社員を探す（@Find）
    public Employee getByCode(String code) {
        return employeeRepository.byCode(code)
                .filter(employee -> Employee.ACTIVE.equals(employee.getStatus()))
                .orElseThrow(EmployeeNotFoundException::new);
    }

    // 在籍中の社員の 1 ページ（@Find と PageRequest）
    public EmployeePage findActive(int page) {
        Page<Employee> found = employeeRepository.byStatus(Employee.ACTIVE,
                PageRequest.ofPage(Math.max(page, 1), PAGE_SIZE, true));
        return new EmployeePage(found.content(),
                (int) found.totalPages(), found.totalElements());
    }

    public EmployeePage searchByNameAndSalary(String keyword,
            int salaryFrom, int salaryTo, int page) {
        if (salaryTo < salaryFrom) {
            throw new RangeException();
        }
        PageRequest request =
                PageRequest.ofPage(Math.max(page, 1), PAGE_SIZE, true);
        Page<Employee> found = employeeRepository.searchByNameAndSalary(
                "%" + keyword + "%", salaryFrom, salaryTo, request);
        return new EmployeePage(found.content(),
                (int) found.totalPages(), found.totalElements());
    }

    // 在籍中（status が active）の社員を取得する。見つからなければ EmployeeNotFoundException
    public Employee get(Integer employeeId) {
        return employeeRepository.findById(employeeId)
                .filter(employee -> Employee.ACTIVE.equals(employee.getStatus()))
                .orElseThrow(EmployeeNotFoundException::new);
    }

    // insert の戻り値から採番された ID を受け取り、社員コードを設定して update する（1 つのトランザクション）
    @Transactional
    public Employee create(Employee employee) {
        employee.setStatus(Employee.ACTIVE);
        employee.setEmployeeCode("E0000");
        Employee saved = employeeRepository.insert(employee);
        // 演習用: Payara 7.2026.9 の insert は INSERT を送る前に戻る（採番された社員 ID が入っていない）ので、
        // 同じトランザクションの永続化コンテキストを flush して ID を確定させる
        entityManager.flush();
        saved.setEmployeeCode(String.format("E%04d", saved.getEmployeeId()));
        return employeeRepository.update(saved);
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
            return employeeRepository.update(current);
        } catch (OptimisticLockingFailureException e) {
            throw new ConflictException();
        }
    }

    // 論理削除。状態を変えたエンティティを update で保存する
    @Transactional
    public void delete(Integer employeeId) {
        Employee current = get(employeeId);
        current.setStatus(Employee.DELETED);
        employeeRepository.update(current);
    }
}
