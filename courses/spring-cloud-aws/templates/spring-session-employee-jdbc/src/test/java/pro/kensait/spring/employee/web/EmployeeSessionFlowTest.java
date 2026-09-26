package pro.kensait.spring.employee.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import pro.kensait.spring.employee.entity.Employee;
import pro.kensait.spring.employee.service.EmployeePage;
import pro.kensait.spring.employee.service.EmployeeService;
import pro.kensait.spring.employee.service.ConflictException;

class EmployeeSessionFlowTest {
    private EmployeeService service;
    private MockMvc mvc;
    private MockHttpSession session;

    @BeforeEach
    void setUp() throws Exception {
        service = mock(EmployeeService.class);
        when(service.departments()).thenReturn(List.of());
        when(service.jobs()).thenReturn(List.of());
        when(service.departmentExists(1)).thenReturn(true);
        when(service.jobExists(1)).thenReturn(true);
        when(service.departmentNameOf(1)).thenReturn("営業部");
        when(service.jobNameOf(1)).thenReturn("一般");
        when(service.search(any(), anyInt())).thenReturn(EmployeePage.empty());
        mvc = MockMvcBuilders.standaloneSetup(new EmployeeController(service)).build();
        session = new MockHttpSession();
        mvc.perform(get("/employees/new").session(session)).andExpect(status().isOk());
    }

    private EmployeeDraft draft() { return (EmployeeDraft) session.getAttribute("employeeDraft"); }

    private void confirm() throws Exception {
        mvc.perform(post("/employees/confirm").session(session)
                .param("draftToken", draft().getToken()).param("employeeName", "Alice")
                .param("departmentId", "1").param("jobId", "1")
                .param("salary", "300000").param("entranceDate", "2026-04-01")
                .param("employeeId", "999").param("version", "999"))
                .andExpect(view().name("EmployeeConfirmPage"));
    }

    @Test
    void confirmationKeepsInputWithoutWritingBusinessData() throws Exception {
        confirm();
        assertEquals("Alice", draft().getInput().getEmployeeName());
        assertTrue(draft().isConfirmed());
        assertNull(draft().getInput().getEmployeeId());
        verify(service, never()).create(any());
        verify(service, never()).update(any(), any(), any());
    }

    @Test
    void saveUsesServerDraftAndClearsOnlyManagedAttribute() throws Exception {
        session.setAttribute("unrelated", "keep");
        confirm();
        mvc.perform(post("/employees/save").session(session)
                .param("draftToken", draft().getToken()).param("employeeName", "tampered"))
                .andExpect(redirectedUrl("/employees"));
        verify(service).create(argThat(e -> "Alice".equals(e.getEmployeeName())));
        assertNull(draft());
        assertEquals("keep", session.getAttribute("unrelated"));
        mvc.perform(post("/employees/save").session(session).param("draftToken", "old"))
                .andExpect(redirectedUrl("/employees/new"));
        verify(service, times(1)).create(any());
    }

    @Test
    void backRetainsValuesAndRequiresConfirmationAgain() throws Exception {
        confirm();
        mvc.perform(get("/employees/draft").session(session)).andExpect(view().name("EmployeeFormPage"));
        assertEquals("Alice", draft().getInput().getEmployeeName());
        assertFalse(draft().isConfirmed());
        mvc.perform(post("/employees/save").session(session).param("draftToken", draft().getToken()))
                .andExpect(redirectedUrl("/employees/draft"));
        verify(service, never()).create(any());
    }

    @Test
    void invalidInputDoesNotBecomeConfirmed() throws Exception {
        mvc.perform(post("/employees/confirm").session(session)
                .param("draftToken", draft().getToken()).param("departmentId", "invalid"))
                .andExpect(view().name("EmployeeFormPage"));
        assertFalse(draft().isConfirmed());
        verify(service, never()).create(any());
    }

    @Test
    void staleTabCannotOverwriteNewDraft() throws Exception {
        String old = draft().getToken();
        mvc.perform(get("/employees/new").session(session));
        mvc.perform(post("/employees/confirm").session(session).param("draftToken", old)
                .param("employeeName", "stale"))
                .andExpect(redirectedUrl("/employees/draft"));
        assertEquals("", draft().getInput().getEmployeeName());
    }

    @Test
    void cancelClearsDraft() throws Exception {
        mvc.perform(post("/employees/cancel").session(session)).andExpect(redirectedUrl("/employees"));
        assertNull(draft());
    }

    @Test
    void expiredSessionReturnsToNewInput() throws Exception {
        mvc.perform(get("/employees/draft")).andExpect(redirectedUrl("/employees/new"));
    }

    @Test
    void updateUsesIdAndVersionLoadedFromDatabase() throws Exception {
        Employee e = new Employee();
        e.setEmployeeId(7); e.setEmployeeName("Before"); e.setVersion(2);
        e.setSalary(200000); e.setEntranceDate(LocalDate.of(2025, 4, 1));
        when(service.get(7)).thenReturn(e);
        mvc.perform(get("/employees/7/edit").session(session));
        confirm();
        mvc.perform(post("/employees/save").session(session).param("draftToken", draft().getToken()))
                .andExpect(redirectedUrl("/employees"));
        verify(service).update(eq(7), argThat(input -> "Alice".equals(input.getEmployeeName())), eq(2));
    }

    @Test
    void conflictPreservesDraftForReview() throws Exception {
        confirm();
        draft().getInput().setEmployeeId(1);
        draft().getInput().setVersion(0);
        when(service.update(eq(1), any(), eq(0))).thenThrow(new ConflictException());
        mvc.perform(post("/employees/save").session(session).param("draftToken", draft().getToken()))
                .andExpect(view().name("EmployeeFormPage"));
        assertEquals("Alice", draft().getInput().getEmployeeName());
        assertFalse(draft().isConfirmed());
    }
}
