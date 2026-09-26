package pro.kensait.spring.employee.rest.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pro.kensait.spring.employee.rest.api.dto.EmployeeTO;
import pro.kensait.spring.employee.rest.entity.Department;
import pro.kensait.spring.employee.rest.entity.Employee;
import pro.kensait.spring.employee.rest.repository.DepartmentRepository;
import pro.kensait.spring.employee.rest.repository.EmployeeRepository;

/*
 * 社員に対するビジネスロジックを表すクラス
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

    // サービスメソッド：社員の取得
    public EmployeeTO getEmployee(Integer employeeId) {
        logger.info("[ EmployeeService#getEmployee ]");

        // 主キー検索を実行し、存在しない場合はEmployeeNotFoundExceptionを送出する
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(
                        "社員が存在しません => " + employeeId));

        // エンティティを転送オブジェクトに詰め替えて返す
        return toEmployeeTO(employee);
    }

    // サービスメソッド：全社員の取得
    public List<EmployeeTO> getEmployeesAll() {
        logger.info("[ EmployeeService#getEmployeesAll ]");
        List<Employee> employeeList = employeeRepository.findAll();
        return toEmployeeTOList(employeeList);
    }

    // サービスメソッド：社員を検索する（部署IDをキーに）
    public List<EmployeeTO> getEmployeesByDepartment(Integer departmentId) {
        logger.info("[ EmployeeService#getEmployeesByDepartment ]");
        List<Employee> employeeList =
                employeeRepository.findByDepartmentId(departmentId);
        return toEmployeeTOList(employeeList);
    }

    // サービスメソッド：社員を検索する（給与範囲をキーに）
    public List<EmployeeTO> getEmployeesBySalary(Integer lowerSalary,
            Integer upperSalary) {
        logger.info("[ EmployeeService#getEmployeesBySalary ]");
        List<Employee> employeeList =
                employeeRepository.findBySalaryRange(lowerSalary, upperSalary);
        return toEmployeeTOList(employeeList);
    }

    // サービスメソッド：社員の追加
    public EmployeeTO createEmployee(EmployeeTO employeeTO) {
        logger.info("[ EmployeeService#createEmployee ]");

        // 転送オブジェクトをエンティティに詰め替えて保存する
        // このとき、IDはDBのIDENTITYによって新規に採番される
        Employee employee = toEmployee(employeeTO);
        employeeRepository.saveAndFlush(employee);

        // 採番されたIDを含むエンティティを転送オブジェクトに詰め替えて返す
        return toEmployeeTO(employee);
    }

    // サービスメソッド：社員の更新
    public EmployeeTO updateEmployee(EmployeeTO employeeTO) {
        logger.info("[ EmployeeService#updateEmployee ]");

        // 更新対象が存在しない場合はEmployeeNotFoundExceptionを送出する
        if (!employeeRepository.existsById(employeeTO.employeeId())) {
            throw new EmployeeNotFoundException(
                    "社員が存在しません => " + employeeTO.employeeId());
        }

        // 転送オブジェクトをエンティティに詰め替えて保存する
        Employee employee = toEmployee(employeeTO);
        employeeRepository.saveAndFlush(employee);
        return toEmployeeTO(employee);
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

    // エンティティを転送オブジェクトに詰め替える
    private EmployeeTO toEmployeeTO(Employee employee) {
        Department department = employee.getDepartment();
        return new EmployeeTO(
                employee.getEmployeeId(),
                employee.getEmployeeName(),
                department == null ? null : department.getDepartmentId(),
                department == null ? null : department.getDepartmentName(),
                employee.getJobName(),
                employee.getSalary(),
                employee.getHireDate());
    }

    // エンティティのリストを転送オブジェクトのリストに詰め替える
    private List<EmployeeTO> toEmployeeTOList(List<Employee> employeeList) {
        List<EmployeeTO> employeeTOList = new ArrayList<>();
        for (Employee employee : employeeList) {
            employeeTOList.add(toEmployeeTO(employee));
        }
        return employeeTOList;
    }

    // 転送オブジェクトをエンティティに詰め替える
    private Employee toEmployee(EmployeeTO employeeTO) {
        // 部署IDから部署エンティティへの参照を取得する
        // 存在しない部署IDの場合は、保存時に外部キー制約違反
        // （DataIntegrityViolationException）が発生する
        Department department =
                departmentRepository.getReferenceById(employeeTO.departmentId());
        return new Employee(
                employeeTO.employeeId(),
                employeeTO.employeeName(),
                department,
                employeeTO.jobName(),
                employeeTO.salary(),
                employeeTO.hireDate());
    }
}
