package pro.kensait.jakartaee.employee.web;

import java.io.Serializable;

import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pro.kensait.jakartaee.employee.service.Employee;
import pro.kensait.jakartaee.employee.service.EmployeeService;

/**
 * 社員の新規入力と編集（入力画面 → 確認画面 → 確定）の値を保持するバッキングBean
 * 会話スコープなので、toCreate・edit で始めた会話が update の end() まで同じインスタンスを使う
 */
@Named
@ConversationScoped
public class EmployeeEditBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private Conversation conversation;
    @Inject
    private EmployeeService employeeService;

    private Integer employeeId;

    @NotEmpty
    @Size(max = 30)
    private String employeeName;

    @NotEmpty
    @Size(max = 30)
    private String departmentName;

    @NotNull
    @Min(0)
    @Max(9_999_999)
    private Integer salary;

    public String toCreate() {
        beginConversation();
        clearInput();
        return "EmployeeInputPage?faces-redirect=true";
    }

    public String edit(Integer id) {
        beginConversation();
        Employee employee = employeeService.getEmployee(id);
        employeeId = employee.getEmployeeId();
        employeeName = employee.getEmployeeName();
        departmentName = employee.getDepartmentName();
        salary = employee.getSalary();
        return "EmployeeInputPage?faces-redirect=true";
    }

    // 入力値の検証は、Faces のライフサイクルがこのメソッドの前に行う（エラーなら入力画面を再表示）
    public String toConfirm() {
        return "EmployeeConfirmPage?faces-redirect=true";
    }

    // 会話に残った値が、そのまま入力欄に表示される
    public String back() {
        return "EmployeeInputPage?faces-redirect=true";
    }

    public String update() {
        Employee employee = new Employee(employeeId, employeeName,
                departmentName, salary);
        if (employeeId == null) {
            employeeService.createEmployee(employee);
        } else {
            employeeService.updateEmployee(employee);
        }
        conversation.end();
        return "EmployeeTablePage?faces-redirect=true";
    }

    private void beginConversation() {
        if (conversation.isTransient()) {
            conversation.begin();
        }
    }

    private void clearInput() {
        employeeId = null;
        employeeName = null;
        departmentName = null;
        salary = null;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Integer getSalary() {
        return salary;
    }

    public void setSalary(Integer salary) {
        this.salary = salary;
    }
}
