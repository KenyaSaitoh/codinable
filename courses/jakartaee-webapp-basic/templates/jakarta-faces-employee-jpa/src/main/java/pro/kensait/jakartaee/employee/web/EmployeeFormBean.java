package pro.kensait.jakartaee.employee.web;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pro.kensait.jakartaee.employee.entity.Department;
import pro.kensait.jakartaee.employee.entity.Employee;
import pro.kensait.jakartaee.employee.entity.Job;
import pro.kensait.jakartaee.employee.service.ConflictException;
import pro.kensait.jakartaee.employee.service.EmployeeNotFoundException;
import pro.kensait.jakartaee.employee.service.EmployeeService;

/**
 * 登録・編集画面（EmployeeFormPage.xhtml）のバッキングBean
 * 編集は EmployeeFormPage.xhtml?employeeId=10001 のように、ビューパラメータの ID で対象を選ぶ
 * 画面を表示したときの version を保持し、更新のときに渡して競合を検出する
 */
@Named
@ViewScoped
public class EmployeeFormBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private EmployeeService employeeService;

    private Integer employeeId;
    private String employeeCode;

    @NotBlank
    @Size(max = 30)
    private String employeeName;

    private Integer departmentId;

    @NotNull
    private Integer jobId;

    @NotNull
    @Min(0)
    @Max(9_999_999)
    private Integer salary;

    @NotNull
    private LocalDate entranceDate;

    private Integer version;

    // f:viewAction。GET で開いたときだけ呼ばれ、編集なら社員を読み込む
    public String load() {
        if (employeeId == null) {
            return null;
        }
        try {
            Employee employee = employeeService.get(employeeId);
            employeeCode = employee.getEmployeeCode();
            employeeName = employee.getEmployeeName();
            departmentId = employee.getDepartment() == null ? null : employee.getDepartment().getDepartmentId();
            jobId = employee.getJob().getJobId();
            salary = employee.getSalary();
            entranceDate = employee.getEntranceDate();
            version = employee.getVersion();
            return null;
        } catch (EmployeeNotFoundException exception) {
            return toList(FacesMessage.SEVERITY_ERROR, exception.getMessage());
        }
    }

    public String save() {
        Employee input = toEmployee();
        try {
            if (employeeId == null) {
                Employee saved = employeeService.create(input);
                return toList(FacesMessage.SEVERITY_INFO,
                        "社員ID " + saved.getEmployeeId() + "（" + saved.getEmployeeCode() + "）を登録しました");
            }
            employeeService.update(employeeId, input, version);
            return toList(FacesMessage.SEVERITY_INFO, "社員ID " + employeeId + " を更新しました");
        } catch (ConflictException | EmployeeNotFoundException exception) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, exception.getMessage(), null));
            return null;
        }
    }

    private Employee toEmployee() {
        Employee employee = new Employee();
        employee.setEmployeeName(employeeName.strip());
        employee.setDepartment(employeeService.department(departmentId));
        employee.setJob(employeeService.job(jobId));
        employee.setSalary(salary);
        employee.setEntranceDate(entranceDate);
        return employee;
    }

    // メッセージをリダイレクト先まで持ち越して（Flash）、一覧画面へ戻る
    private String toList(FacesMessage.Severity severity, String text) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(severity, text, null));
        context.getExternalContext().getFlash().setKeepMessages(true);
        return "EmployeeTablePage?faces-redirect=true";
    }

    public List<Department> getDepartments() {
        return employeeService.departments();
    }

    public List<Job> getJobs() {
        return employeeService.jobs();
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public Integer getSalary() {
        return salary;
    }

    public void setSalary(Integer salary) {
        this.salary = salary;
    }

    public LocalDate getEntranceDate() {
        return entranceDate;
    }

    public void setEntranceDate(LocalDate entranceDate) {
        this.entranceDate = entranceDate;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getVersion() {
        return version;
    }
}
