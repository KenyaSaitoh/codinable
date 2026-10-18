package pro.kensait.jakartaee.employee.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * EmployeeService は依存先の EmployeeDAO をコンストラクタで受け取るので、
 * コンテナが無くても new EmployeeService(dao) の形でテストできる
 */
class EmployeeServiceTest {
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        EmployeeDAO dao = new EmployeeDAO();
        dao.init(); // コンテナの外なので @PostConstruct は自分で呼ぶ
        employeeService = new EmployeeService(dao);
    }

    @Test
    void getEmployeesAll_returns16EmployeesInIdOrder() {
        var list = employeeService.getEmployeesAll();
        assertEquals(16, list.size());
        assertEquals("Alice", list.get(0).getEmployeeName());
        assertEquals("Trent", list.get(15).getEmployeeName());
    }

    @Test
    void createEmployee_assignsNextId() {
        Employee created = employeeService.createEmployee(
                new Employee(null, "Zoe", "SALES", 300_000));
        assertEquals(10017, created.getEmployeeId());
        assertEquals("Zoe", employeeService.getEmployee(10017).getEmployeeName());
    }

    @Test
    void updateEmployee_returnsZeroWhenNotFound() {
        assertEquals(1, employeeService.updateEmployee(
                new Employee(10001, "Alice", "HR", 510_000)));
        assertEquals("HR", employeeService.getEmployee(10001).getDepartmentName());
        assertEquals(0, employeeService.updateEmployee(
                new Employee(99999, "Nobody", "HR", 1)));
    }

    @Test
    void removeEmployee_deletesFromMap() {
        assertEquals(1, employeeService.removeEmployee(10003));
        assertNull(employeeService.getEmployee(10003));
        assertEquals(0, employeeService.removeEmployee(10003));
    }
}
