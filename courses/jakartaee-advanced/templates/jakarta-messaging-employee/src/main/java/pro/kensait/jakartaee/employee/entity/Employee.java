package pro.kensait.jakartaee.employee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/*
 * 社員のエンティティ（送信側の EMPLOYEE テーブル。新しい社員には 10017 から番号が振られる）
 */
@Entity
@Table(name = "EMPLOYEE")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }
    public Integer getSalary() { return salary; }
    public void setSalary(Integer salary) { this.salary = salary; }
    public LocalDate getEntranceDate() { return entranceDate; }
    public void setEntranceDate(LocalDate entranceDate) { this.entranceDate = entranceDate; }
}
