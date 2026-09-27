package pro.kensait.spring.employee.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import pro.kensait.spring.employee.service.Employee;
import pro.kensait.spring.employee.service.EmployeeService;

/*
 * 社員のテスト
 */
@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {
    @Mock
    private EmployeeService employeeService;

    @Mock
    private MessageSource messageSource;

    private MockMvc mockMvc;

    // 各テストケースで共通的な前処理
    @BeforeEach
    void setUp() {
        EmployeeController controller = new EmployeeController(employeeService, messageSource);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // 「トップ画面から一覧画面へのリダイレクト」の検証
    @Test
    void redirectsTopToList() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/viewList"));
    }

    // 「社員一覧の表示」の検証
    @Test
    void showsEmployeeList() throws Exception {
        List<Employee> employees = List.of(
                new Employee(10001, "Alice", "SALES", 500_000));
        when(employeeService.getEmployeesAll()).thenReturn(employees);

        mockMvc.perform(get("/viewList"))
                .andExpect(status().isOk())
                .andExpect(view().name("EmployeeTablePage"))
                .andExpect(model().attribute("employeeList", employees));
    }

    // 「有効な社員情報の確認」の検証
    @Test
    void confirmsValidEmployee() throws Exception {
        mockMvc.perform(post("/toConfirm")
                        .param("employeeName", "Walter")
                        .param("departmentName", "SALES")
                        .param("salary", "230000"))
                .andExpect(status().isOk())
                .andExpect(view().name("EmployeeUpdatePage"))
                .andExpect(model().hasNoErrors());
    }

    // 「クエリパラメータで選択した社員の表示」の検証
    @Test
    void showsEmployeeSelectedByQueryParameter() throws Exception {
        Employee employee = new Employee(10001, "Alice", "SALES", 500_000);
        when(employeeService.getEmployee(10001)).thenReturn(employee);

        mockMvc.perform(get("/employees/by-query").param("employeeId", "10001"))
                .andExpect(status().isOk())
                .andExpect(view().name("EmployeeDetailPage"))
                .andExpect(model().attribute("employee", employee))
                .andExpect(model().attribute("parameterType", "クエリパラメータ"));
    }

    // 「パス変数で選択した社員の表示」の検証
    @Test
    void showsEmployeeSelectedByPathVariable() throws Exception {
        Employee employee = new Employee(10002, "Bob", "PLANNING", 450_000);
        when(employeeService.getEmployee(10002)).thenReturn(employee);

        mockMvc.perform(get("/employees/10002"))
                .andExpect(status().isOk())
                .andExpect(view().name("EmployeeDetailPage"))
                .andExpect(model().attribute("employee", employee))
                .andExpect(model().attribute("parameterType", "パス変数"));
    }
}
