package pro.kensait.spring.employee.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/*
 * 社員のテスト
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock
    private EmployeeDAO employeeDao;

    @InjectMocks
    private EmployeeService employeeService;

    // 「全社員の取得」の検証
    @Test
    void returnsAllEmployees() {
        List<Employee> expected = List.of(
                new Employee(10001, "Alice", "SALES", 500_000));
        when(employeeDao.findAll()).thenReturn(expected);

        assertEquals(expected, employeeService.getEmployeesAll());
    }

    // 「次のIDを使用した社員登録」の検証
    @Test
    void createsEmployeeWithNextId() {
        Employee employee = new Employee("Walter", "SALES", 230_000);
        when(employeeDao.getMaxEmployeeId()).thenReturn(10016);

        Employee created = employeeService.createEmployee(employee);

        assertEquals(10017, created.getEmployeeId());
        verify(employeeDao).save(employee);
    }
}
