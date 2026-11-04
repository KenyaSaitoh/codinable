package pro.kensait.spring.employee.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.OptimisticLockException;
import pro.kensait.spring.employee.entity.Department;
import pro.kensait.spring.employee.entity.Employee;
import pro.kensait.spring.employee.entity.EmployeeSearchCriteria;
import pro.kensait.spring.employee.entity.Job;
import pro.kensait.spring.employee.repository.DepartmentRepository;
import pro.kensait.spring.employee.repository.EmployeeRepository;
import pro.kensait.spring.employee.repository.JobRepository;
/** 社員管理の業務ロジック */
@Service
public class EmployeeService {
    public static final int PAGE_SIZE = 5;

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final JobRepository jobRepository;

    // コンストラクタ
    public EmployeeService(EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            JobRepository jobRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.jobRepository = jobRepository;
    }

    // 部署一覧を部署ID順で取得
    public List<Department> departments() {
        return departmentRepository.findAll(Sort.by("departmentId"));
    }

    // 役職一覧を等級順で取得
    public List<Job> jobs() {
        return jobRepository.findAll(Sort.by("grade"));
    }

    // 指定した部署の取得（部署なし、または存在しない部署は null）
    public Department department(Integer departmentId) {
        return departmentId == null ? null
                : departmentRepository.findById(departmentId).orElse(null);
    }

    // 指定した役職の取得（存在しない役職は null）
    public Job job(Integer jobId) {
        return jobId == null ? null : jobRepository.findById(jobId).orElse(null);
    }

    // 指定された条件とページ番号で社員の検索
    public EmployeePage search(EmployeeSearchCriteria criteria, int page) {
        if (criteria.rangeReversed()) {
            throw new RangeException();
        }
        int index = Math.max(page, 1) - 1;
        var found = employeeRepository.search(criteria,
                PageRequest.of(index, PAGE_SIZE, Sort.by("employeeId")));
        return new EmployeePage(found.getContent(), found.getTotalPages(),
                found.getTotalElements());
    }

    // 指定した在籍中の社員の取得
    public Employee get(Integer employeeId) {
        return employeeRepository.findById(employeeId)
                .filter(employee -> Employee.ACTIVE.equals(employee.getStatus()))
                .orElseThrow(NotFoundException::new);
    }

    // 社員を登録し、採番されたIDから社員コードの設定
    // （version は @Version なので設定しない。INSERT のときに永続化プロバイダーが 0 を入れる）
    @Transactional
    public Employee create(Employee employee) {
        employee.setStatus(Employee.ACTIVE);
        employee.setEmployeeCode("E0000");
        Employee saved = employeeRepository.save(employee);
        saved.setEmployeeCode(String.format("E%04d", saved.getEmployeeId()));
        return saved;
    }

    // バージョンが一致する場合に社員情報の更新
    // 画面から来た version が現在の version と違えば競合。さらに flush のときに
    // 永続化プロバイダーが検出した競合（OptimisticLockException）も ConflictException に変換する
    @Transactional
    public Employee update(Integer employeeId, Employee input, Integer version) {
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
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
            throw new ConflictException();
        }
        return current;
    }

    // 指定した社員を論理削除
    // （managed のエンティティの状態を変えるだけで、コミット時に UPDATE が発行される）
    @Transactional
    public void delete(Integer employeeId) {
        Employee current = get(employeeId);
        current.setStatus(Employee.DELETED);
    }
}
