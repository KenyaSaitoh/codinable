package pro.kensait.jakartaee.employee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

/*
 * 社員のエンティティ（EMPLOYEE テーブル）
 */
@Entity
@Table(name = "EMPLOYEE")
public class Employee {
    @Id
    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;
    @Column(name = "EMPLOYEE_NAME")
    private String employeeName;
    @ManyToOne
    @JoinColumn(name = "DEPARTMENT_ID")
    private Department department;
    @Column(name = "JOB_NAME")
    private String jobName;
    @Column(name = "SALARY")
    private Integer salary;
    @Column(name = "ENTRANCE_DATE")
    private LocalDate entranceDate;

    public Integer getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public Department getDepartment() { return department; }
    public String getJobName() { return jobName; }
    public Integer getSalary() { return salary; }
    public LocalDate getEntranceDate() { return entranceDate; }
}
