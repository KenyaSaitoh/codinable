package pro.kensait.spring.employee.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import pro.kensait.spring.employee.entity.Department;
import pro.kensait.spring.employee.entity.Employee;
import pro.kensait.spring.employee.entity.EmployeeSearchCriteria;
import pro.kensait.spring.employee.entity.Job;
import pro.kensait.spring.employee.mapper.DepartmentMapper;
import pro.kensait.spring.employee.mapper.EmployeeMapper;
import pro.kensait.spring.employee.mapper.JobMapper;
/** 社員管理の業務ロジック */
@Service
public class EmployeeService {
    public static final int PAGE_SIZE = 5;

    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    private final JobMapper jobMapper;

    // コンストラクタ
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "Spring manages the injected collaborators")
    public EmployeeService(EmployeeMapper employeeMapper,
            DepartmentMapper departmentMapper, JobMapper jobMapper) {
        this.employeeMapper = employeeMapper;
        this.departmentMapper = departmentMapper;
        this.jobMapper = jobMapper;
    }

    // 部署一覧を部署ID順で取得
    public List<Department> departments() {
        return departmentMapper.findAll();
    }

    // 役職一覧を等級順で取得
    public List<Job> jobs() {
        return jobMapper.findAll();
    }

    // 指定された条件とページ番号で社員の検索
    public EmployeePage search(EmployeeSearchCriteria criteria, int page) {
        if (criteria.rangeReversed()) {
            throw new RangeException();
        }
        int current = Math.max(page, 1);
        long total = employeeMapper.count(criteria);
        int totalPages = (int) ((total + PAGE_SIZE - 1) / PAGE_SIZE);
        List<Employee> employees = employeeMapper.search(criteria,
                (current - 1) * PAGE_SIZE, PAGE_SIZE);
        return new EmployeePage(employees, totalPages, total);
    }

    // 指定した在籍中の社員の取得
    public Employee get(Integer employeeId) {
        return Optional.ofNullable(employeeMapper.findById(employeeId))
                .filter(employee -> Employee.ACTIVE.equals(employee.getStatus()))
                .orElseThrow(NotFoundException::new);
    }

    // 社員を登録し、採番されたIDから社員コードの設定
    @Transactional
    public Employee create(Employee employee) {
        employee.setStatus(Employee.ACTIVE);
        employee.setVersion(0);
        employee.setEmployeeCode("E0000");
        employeeMapper.insert(employee);
        employee.setEmployeeCode(String.format("E%04d", employee.getEmployeeId()));
        employeeMapper.updateCode(employee);
        return employee;
    }

    // バージョンが一致する場合に社員情報の更新
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
        current.setVersion(current.getVersion() + 1);
        employeeMapper.update(current);
        return current;
    }

    // 指定した社員を論理削除
    @Transactional
    public void delete(Integer employeeId) {
        Employee current = get(employeeId);
        current.setStatus(Employee.DELETED);
        employeeMapper.updateStatus(current);
    }

    // 指定した部署が存在するか確認
    public boolean departmentExists(Integer departmentId) {
        return departmentId != null && departmentMapper.findById(departmentId) != null;
    }

    // 指定した役職が存在するか確認
    public boolean jobExists(Integer jobId) {
        return jobId != null && jobMapper.findById(jobId) != null;
    }

    // 指定した部署の名称の取得
    public String departmentNameOf(Integer departmentId) {
        return Optional.ofNullable(departmentMapper.findById(departmentId))
                .map(Department::departmentName).orElse("");
    }

    // 指定した役職の名称の取得
    public String jobNameOf(Integer jobId) {
        return Optional.ofNullable(jobMapper.findById(jobId)).map(Job::jobName).orElse("");
    }
}
