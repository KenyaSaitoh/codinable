package pro.kensait.spring.employee.web;

import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pro.kensait.spring.employee.entity.Employee;
import pro.kensait.spring.employee.entity.EmployeeSearchCriteria;
import pro.kensait.spring.employee.service.ConflictException;
import pro.kensait.spring.employee.service.EmployeePage;
import pro.kensait.spring.employee.service.EmployeeService;
import pro.kensait.spring.employee.service.NotFoundException;

/*
 * 社員コントローラーWebのテスト
 */
@WebMvcTest(EmployeeController.class)
class EmployeeControllerWebTest {
    @Autowired private MockMvc mvc;
    @MockitoBean private EmployeeService service;

    // テスト前処理
    @BeforeEach
    void emptySearchResult() {
        when(service.search(any(), anyInt())).thenReturn(EmployeePage.empty());
    }

    // 「検索キーワードの正規化と条件のバインド」の検証
    @Test
    void normalizesSearchKeywordAndBindsConditions() throws Exception {
        mvc.perform(get("/employees").param("keyword", "  Alice  ")
                .param("departmentId", "1").param("salaryFrom", "300000").param("page", "2"))
                .andExpect(status().isOk()).andExpect(view().name("EmployeeListPage"));
        var criteria = ArgumentCaptor.forClass(EmployeeSearchCriteria.class);
        verify(service).search(criteria.capture(), eq(2));
        org.assertj.core.api.Assertions.assertThat(criteria.getValue().getKeyword()).isEqualTo("Alice");
        org.assertj.core.api.Assertions.assertThat(criteria.getValue().getDepartmentId()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(criteria.getValue().getSalaryFrom()).isEqualTo(300000);
    }

    // 「登録画面の表示」の検証
    @Test
    void opensRegistrationForm() throws Exception {
        mvc.perform(get("/employees/new")).andExpect(status().isOk())
                .andExpect(view().name("EmployeeFormPage"))
                .andExpect(model().attributeExists("employeeParam", "departments", "jobs"));
    }

    // 「不正入力時の未保存とフォーム再表示」の検証
    @Test
    void invalidInputReturnsFormWithoutSaving() throws Exception {
        mvc.perform(post("/employees").param("employeeName", ""))
                .andExpect(status().isOk()).andExpect(view().name("EmployeeFormPage"))
                .andExpect(model().attribute("errors", contains("氏名を入力してください", "部署を選んでください",
                        "役職を選んでください", "月給は0以上9999999以下で入力してください", "入社日を入力してください")));
        verify(service, never()).create(any());
    }

    // 「有効な入力の変換と保存」の検証
    @Test
    void validInputIsConvertedAndSaved() throws Exception {
        when(service.departmentExists(1)).thenReturn(true);
        when(service.jobExists(2)).thenReturn(true);
        mvc.perform(post("/employees").param("employeeName", "  Alice  ")
                .param("departmentId", "1").param("jobId", "2")
                .param("salary", "300000").param("entranceDate", "2026-04-01"))
                .andExpect(status().isOk()).andExpect(view().name("EmployeeListPage"));
        var employee = ArgumentCaptor.forClass(Employee.class);
        verify(service).create(employee.capture());
        org.assertj.core.api.Assertions.assertThat(employee.getValue().getEmployeeName()).isEqualTo("Alice");
        org.assertj.core.api.Assertions.assertThat(employee.getValue().getJobId()).isEqualTo(2);
    }

    // 「競合時のメッセージ付きフォーム再表示」の検証
    @Test
    void staleUpdateReturnsFormWithConflictMessage() throws Exception {
        when(service.departmentExists(1)).thenReturn(true);
        when(service.jobExists(2)).thenReturn(true);
        when(service.update(eq(11), any(), eq(0))).thenThrow(new ConflictException());
        mvc.perform(post("/employees/11").param("employeeName", "Bob")
                .param("departmentId", "1").param("jobId", "2").param("version", "0")
                .param("salary", "300000").param("entranceDate", "2026-04-01"))
                .andExpect(status().isOk()).andExpect(view().name("EmployeeFormPage"))
                .andExpect(model().attribute("errors", contains(new ConflictException().getMessage())));
    }

    // 「未存在社員のメッセージ付き一覧表示」の検証
    @Test
    void missingEmployeeReturnsListWithMessage() throws Exception {
        when(service.get(999)).thenThrow(new NotFoundException());
        mvc.perform(get("/employees/999/edit")).andExpect(status().isOk())
                .andExpect(view().name("EmployeeListPage"))
                .andExpect(model().attribute("errors", contains(new NotFoundException().getMessage())));
    }
}
