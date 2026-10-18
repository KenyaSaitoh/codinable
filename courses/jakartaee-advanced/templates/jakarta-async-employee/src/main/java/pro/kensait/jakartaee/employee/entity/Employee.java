package pro.kensait.jakartaee.employee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/*
 * 社員のエンティティ（EMPLOYEE テーブル）
 * 部署は ID だけを持つ（部署名は DepartmentSummary・EmployeeView が部署 ID から決める）
 */
@Entity
@Table(name = "EMPLOYEE")
public class Employee {
    @Id
    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;
    @Column(name = "EMPLOYEE_NAME")
    private String employeeName;
    @Column(name = "DEPARTMENT_ID")
    private Integer departmentId;
    @Column(name = "JOB_NAME")
    private String jobName;
    @Column(name = "SALARY")
    private Integer salary;
    @Column(name = "ENTRANCE_DATE")
    private LocalDate entranceDate;

    public Integer getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public String getJobName() { return jobName; }
    public Integer getSalary() { return salary; }
    public LocalDate getEntranceDate() { return entranceDate; }
}
