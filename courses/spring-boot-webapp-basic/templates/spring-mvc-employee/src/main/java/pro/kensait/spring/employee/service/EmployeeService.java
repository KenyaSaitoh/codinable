package pro.kensait.spring.employee.service;

import java.util.List;

import org.springframework.stereotype.Service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
/** 社員管理の業務ロジック */
@Service
public class EmployeeService {
    private final EmployeeDAO employeeDao;

    // 社員の初期化
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "Spring manages the injected collaborator")
    public EmployeeService(EmployeeDAO employeeDao) {
        this.employeeDao = employeeDao;
    }

    // サービスメソッド：社員の取得
    public Employee getEmployee(int employeeId) {
        return employeeDao.find(employeeId);
    }

    // サービスメソッド：全社員の取得
    public List<Employee> getEmployeesAll() {
        return employeeDao.findAll();
    }

    // 社員下限月給の取得
    public List<Employee> getEmployeesByLowerSalary(int lowerSalary) {
        return employeeDao.findByLowerSalary(lowerSalary);
    }

    // サービスメソッド：社員の追加
    public Employee createEmployee(Employee employee) {
        employee.setEmployeeId(employeeDao.getMaxEmployeeId() + 1);
        employeeDao.save(employee);
        return employee;
    }

    // サービスメソッド：社員の削除
    public int removeEmployee(Integer employeeId) {
        return employeeDao.delete(employeeId);
    }

    // サービスメソッド：社員の更新
    public int updateEmployee(Employee employee) {
        return employeeDao.update(employee);
    }
}
