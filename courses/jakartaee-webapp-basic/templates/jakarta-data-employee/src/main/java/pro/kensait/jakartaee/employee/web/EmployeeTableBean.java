package pro.kensait.jakartaee.employee.web;

import java.io.Serializable;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import pro.kensait.jakartaee.employee.entity.Employee;
import pro.kensait.jakartaee.employee.service.EmployeeNotFoundException;
import pro.kensait.jakartaee.employee.service.EmployeePage;
import pro.kensait.jakartaee.employee.service.EmployeeService;
import pro.kensait.jakartaee.employee.service.RangeException;

/**
 * 一覧画面（EmployeeTablePage.xhtml）のバッキングBean
 * 検索は Jakarta Data の @Query（氏名の部分一致と月給の範囲）で、条件は固定。空欄は「すべて」として渡す
 */
@Named
@ViewScoped
public class EmployeeTableBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int SALARY_MAX = 9_999_999;

    @Inject
    private EmployeeService employeeService;

    private String keyword;
    private Integer salaryFrom;
    private Integer salaryTo;
    private String employeeCode;
    private int page = 1;
    private EmployeePage found = EmployeePage.empty();
    private List<EmployeeRow> rows = List.of();

    @PostConstruct
    public void init() {
        load();
    }

    public String search() {
        page = 1;
        load();
        return null;
    }

    public String next() {
        page++;
        load();
        return null;
    }

    public String prev() {
        page = Math.max(page - 1, 1);
        load();
        return null;
    }

    // 社員コードで探して、編集画面を開く（@Find の byCode）
    public String openByCode() {
        try {
            Employee employee = employeeService.getByCode(employeeCode == null ? "" : employeeCode.strip());
            return "EmployeeFormPage?faces-redirect=true&employeeId=" + employee.getEmployeeId();
        } catch (EmployeeNotFoundException exception) {
            addMessage(FacesMessage.SEVERITY_ERROR, exception.getMessage());
            return null;
        }
    }

    public String delete(Integer employeeId) {
        try {
            employeeService.delete(employeeId);
            addMessage(FacesMessage.SEVERITY_INFO, "社員ID " + employeeId + " を削除しました");
        } catch (EmployeeNotFoundException exception) {
            addMessage(FacesMessage.SEVERITY_ERROR, exception.getMessage());
        }
        load();
        return null;
    }

    private void load() {
        try {
            found = employeeService.searchByNameAndSalary(
                    keyword == null ? "" : keyword.strip(),
                    salaryFrom == null ? 0 : salaryFrom,
                    salaryTo == null ? SALARY_MAX : salaryTo, page);
        } catch (RangeException exception) {
            addMessage(FacesMessage.SEVERITY_ERROR, exception.getMessage());
            found = EmployeePage.empty();
        }
        rows = found.content().stream().map(this::toRow).toList();
    }

    private EmployeeRow toRow(Employee employee) {
        return new EmployeeRow(employee.getEmployeeId(), employee.getEmployeeCode(),
                employee.getEmployeeName(),
                employee.getDepartment() == null ? "" : employee.getDepartment().getDepartmentName(),
                employee.getJob().getJobName(),
                employee.getSalary(), employee.getEntranceDate());
    }

    private void addMessage(FacesMessage.Severity severity, String text) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, text, null));
    }

    public long getActiveCount() {
        return employeeService.activeCount();
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getSalaryFrom() {
        return salaryFrom;
    }

    public void setSalaryFrom(Integer salaryFrom) {
        this.salaryFrom = salaryFrom;
    }

    public Integer getSalaryTo() {
        return salaryTo;
    }

    public void setSalaryTo(Integer salaryTo) {
        this.salaryTo = salaryTo;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public List<EmployeeRow> getRows() {
        return rows;
    }

    public int getPage() {
        return page;
    }

    public int getTotalPages() {
        return found.totalPages();
    }

    public long getTotalCount() {
        return found.totalElements();
    }

    public boolean isHasPrev() {
        return page > 1;
    }

    public boolean isHasNext() {
        return page < found.totalPages();
    }
}
