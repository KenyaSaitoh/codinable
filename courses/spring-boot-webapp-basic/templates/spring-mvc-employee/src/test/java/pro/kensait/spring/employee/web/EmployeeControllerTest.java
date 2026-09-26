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
                new Employee(1, "山田 太郎", "営業部", 320_000));
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
                        .param("employeeName", "新人 太郎")
                        .param("departmentName", "開発部")
                        .param("salary", "250000"))
                .andExpect(status().isOk())
                .andExpect(view().name("EmployeeUpdatePage"))
                .andExpect(model().hasNoErrors());
    }

    // 「クエリパラメータで選択した社員の表示」の検証
    @Test
    void showsEmployeeSelectedByQueryParameter() throws Exception {
        Employee employee = new Employee(1, "山田 太郎", "営業部", 320_000);
        when(employeeService.getEmployee(1)).thenReturn(employee);

        mockMvc.perform(get("/employees/by-query").param("employeeId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("EmployeeDetailPage"))
                .andExpect(model().attribute("employee", employee))
                .andExpect(model().attribute("parameterType", "クエリパラメータ"));
    }

    // 「パス変数で選択した社員の表示」の検証
    @Test
    void showsEmployeeSelectedByPathVariable() throws Exception {
        Employee employee = new Employee(2, "佐藤 花子", "開発部", 450_000);
        when(employeeService.getEmployee(2)).thenReturn(employee);

        mockMvc.perform(get("/employees/2"))
                .andExpect(status().isOk())
                .andExpect(view().name("EmployeeDetailPage"))
                .andExpect(model().attribute("employee", employee))
                .andExpect(model().attribute("parameterType", "パス変数"));
    }
}
