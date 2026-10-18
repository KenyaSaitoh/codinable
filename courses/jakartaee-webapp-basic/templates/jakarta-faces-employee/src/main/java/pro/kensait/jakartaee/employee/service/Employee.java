package pro.kensait.jakartaee.employee.service;

import java.io.Serializable;

/**
 * 社員（ID・氏名・部署名・月給）
 * データベースへ保存するエンティティではなく、メモリー上のデータ
 * 画面の式（#{employee.employeeName} など）は getter を呼んで値を取り出す
 */
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer employeeId;
    private String employeeName;
    private String departmentName;
    private Integer salary;

    public Employee() {
    }

    public Employee(Integer employeeId, String employeeName, String departmentName,
            Integer salary) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.departmentName = departmentName;
        this.salary = salary;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
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
