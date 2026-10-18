package pro.kensait.jakartaee.employee.web;

import java.io.Serializable;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import pro.kensait.jakartaee.employee.entity.Department;
import pro.kensait.jakartaee.employee.entity.Employee;
import pro.kensait.jakartaee.employee.entity.EmployeeSearchCriteria;
import pro.kensait.jakartaee.employee.entity.Job;
import pro.kensait.jakartaee.employee.service.EmployeeNotFoundException;
import pro.kensait.jakartaee.employee.service.EmployeePage;
import pro.kensait.jakartaee.employee.service.EmployeeService;
import pro.kensait.jakartaee.employee.service.RangeException;

/**
 * 一覧画面（EmployeeTablePage.xhtml）のバッキングBean
 * 検索条件とページ番号は、同じ画面で検索・ページ送りを続ける間だけ保持する（@ViewScoped）
 */
@Named
@ViewScoped
public class EmployeeTableBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private EmployeeService employeeService;

    private final EmployeeSearchCriteria criteria = new EmployeeSearchCriteria();
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
            found = employeeService.search(criteria.normalized(), page);
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

    public List<Department> getDepartments() {
        return employeeService.departments();
    }

    public List<Job> getJobs() {
        return employeeService.jobs();
    }

    public EmployeeSearchCriteria getCriteria() {
        return criteria;
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
