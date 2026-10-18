package pro.kensait.jakartaee.employee.service;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/** 社員管理の業務処理（ビジネス層）。データの保持は EmployeeDAO に任せる */
@ApplicationScoped
public class EmployeeService {
    private final EmployeeDAO employeeDao;

    @Inject
    public EmployeeService(EmployeeDAO employeeDao) {
        this.employeeDao = employeeDao;
    }

    protected EmployeeService() {
        this.employeeDao = null; // クライアントプロキシ用
    }

    public Employee getEmployee(int employeeId) {
        return employeeDao.find(employeeId);
    }

    public List<Employee> getEmployeesAll() {
        return employeeDao.findAll();
    }

    // 最大の社員ID に 1 を足して採番し、保存する
    public Employee createEmployee(Employee employee) {
        employee.setEmployeeId(employeeDao.getMaxEmployeeId() + 1);
        employeeDao.save(employee);
        return employee;
    }

    public int updateEmployee(Employee employee) {
        return employeeDao.update(employee);
    }

    public int removeEmployee(Integer employeeId) {
        return employeeDao.delete(employeeId);
    }
}
