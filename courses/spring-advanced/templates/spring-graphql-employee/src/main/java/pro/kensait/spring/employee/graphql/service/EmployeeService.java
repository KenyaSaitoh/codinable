package pro.kensait.spring.employee.graphql.service;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pro.kensait.spring.employee.graphql.entity.Department;
import pro.kensait.spring.employee.graphql.entity.Employee;
import pro.kensait.spring.employee.graphql.repository.DepartmentRepository;
import pro.kensait.spring.employee.graphql.repository.EmployeeRepository;

/*
 * 社員に対するビジネスロジックを表すクラス
 * （GraphQLはスキーマ駆動でレスポンス形状が決まるため、REST版のような
 * 転送オブジェクトへの詰め替えは行わず、エンティティをそのまま返す）
 */
@Service
@Transactional
public class EmployeeService {
    private static final Logger logger = LoggerFactory.getLogger(
            EmployeeService.class);

    // インジェクションポイント
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    // コンストラクタ
    public EmployeeService(EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    // 社員による部署の実行
    @Transactional(readOnly = true)
    public java.util.Map<Department, List<Employee>> employeesByDepartments(List<Department> departments) {
        List<Employee> employees = employeeRepository.findByDepartmentIds(
                departments.stream().map(Department::getDepartmentId).toList());
        java.util.Map<Department, List<Employee>> result = new java.util.LinkedHashMap<>();
        for (Department department : departments) {
            result.put(department, employees.stream().filter(employee ->
                    employee.getDepartment().getDepartmentId().equals(department.getDepartmentId())).toList());
        }
        return result;
    }

    // サービスメソッド：社員の取得
    public Employee getEmployee(Integer employeeId) {
        logger.info("[ EmployeeService#getEmployee ]");

        // 主キー検索を実行し、存在しない場合はEmployeeNotFoundExceptionを送出する
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(
                        "社員が存在しません => " + employeeId));
    }

    // サービスメソッド：全社員の取得
    public List<Employee> getEmployeesAll() {
        logger.info("[ EmployeeService#getEmployeesAll ]");
        return employeeRepository.findAll();
    }

    // サービスメソッド：社員を検索する（部署IDをキーに）
    public List<Employee> getEmployeesByDepartment(Integer departmentId) {
        logger.info("[ EmployeeService#getEmployeesByDepartment ]");
        return employeeRepository.findByDepartmentId(departmentId);
    }

    // サービスメソッド：社員の追加
    public Employee createEmployee(String employeeName, Integer departmentId,
            String jobName, Integer salary, LocalDate entranceDate) {
        logger.info("[ EmployeeService#createEmployee ]");

        // 部署IDから部署エンティティへの参照を取得する
        // 存在しない部署IDの場合は、保存時に外部キー制約違反
        // （DataIntegrityViolationException）が発生する
        Department department = departmentRepository.getReferenceById(departmentId);

        // エンティティを生成して保存する
        // このとき、IDはDBのIDENTITYによって新規に採番される
        Employee employee = new Employee(employeeName, department, jobName, salary,
                entranceDate);
        employeeRepository.saveAndFlush(employee);

        // 採番されたIDを含むエンティティを返す
        return employee;
    }

    // サービスメソッド：社員の更新
    public Employee updateEmployee(Integer employeeId, String employeeName,
            Integer departmentId, String jobName, Integer salary, LocalDate entranceDate) {
        logger.info("[ EmployeeService#updateEmployee ]");

        // 更新対象が存在しない場合はEmployeeNotFoundExceptionを送出する
        if (!employeeRepository.existsById(employeeId)) {
            throw new EmployeeNotFoundException(
                    "社員が存在しません => " + employeeId);
        }

        // 部署IDから部署エンティティへの参照を取得し、エンティティを保存する
        Department department = departmentRepository.getReferenceById(departmentId);
        Employee employee = new Employee(employeeId, employeeName, department, jobName,
                salary, entranceDate);
        employeeRepository.saveAndFlush(employee);
        return employee;
    }

    // サービスメソッド：社員の削除
    public void removeEmployee(Integer employeeId) {
        logger.info("[ EmployeeService#removeEmployee ]");

        // 削除対象が存在しない場合はEmployeeNotFoundExceptionを送出する
        if (!employeeRepository.existsById(employeeId)) {
            throw new EmployeeNotFoundException(
                    "社員が存在しません => " + employeeId);
        }
        employeeRepository.deleteById(employeeId);
    }
}
