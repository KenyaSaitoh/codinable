package pro.kensait.jakartaee.employee.web;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import pro.kensait.jakartaee.employee.service.Employee;
import pro.kensait.jakartaee.employee.service.EmployeeService;

/*
 * 削除の確認画面（employees/delete.xhtml）のバッキングBean
 * この画面は faces-config.xml の protected-views で保護している（トークンの無い URL では開けない）
 */
@Named
@ViewScoped
public class EmployeeDeleteBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private EmployeeService service;

    private Integer id;
    private Employee employee;

    // f:viewAction から呼ばれる（ビューパラメータ id の社員を読む）
    public void load() {
        employee = service.find(id);
    }

    public String delete() {
        service.delete(employee.employeeId());
        return "/employees/list.xhtml?faces-redirect=true";
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Employee getEmployee() { return employee; }
}
