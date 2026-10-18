package pro.kensait.jakartaee.employee.web;

import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import pro.kensait.jakartaee.employee.service.Employee;
import pro.kensait.jakartaee.employee.service.EmployeeService;

@Named
@RequestScoped
public class EmployeeTableBean {
    @Inject
    private EmployeeService employeeService;

    private List<Employee> employeeList;

    @PostConstruct
    public void init() {
        employeeList = employeeService.getEmployeesAll();
    }

    public String remove(Integer employeeId) {
        employeeService.removeEmployee(employeeId);
        return "EmployeeTablePage?faces-redirect=true";
    }

    public List<Employee> getEmployeeList() {
        return employeeList;
    }
}
