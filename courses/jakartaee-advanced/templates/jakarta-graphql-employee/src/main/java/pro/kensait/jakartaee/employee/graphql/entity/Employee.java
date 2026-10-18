package pro.kensait.jakartaee.employee.graphql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.NonNull;

/*
 * 社員のエンティティ。GraphQL の Employee 型にもなる（コードファースト）
 */
@Entity
@Table(name = "EMPLOYEE")
public class Employee {
    @jakarta.persistence.Id // 【1】
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMPLOYEE_ID")
    @Name("id") @Id @NonNull // 【2】
    private Integer employeeId;
    @Column(name = "EMPLOYEE_NAME")
    @NonNull // 【3】
    private String employeeName;
    @ManyToOne
    @JoinColumn(name = "DEPARTMENT_ID")
    private Department department; // 【4】
    @Column(name = "JOB_NAME")
    private String jobName;
    @Column(name = "SALARY")
    private Integer salary;
    @Column(name = "ENTRANCE_DATE")
    private LocalDate entranceDate; // 【5】

    public Employee() {
    }

    public Employee(String employeeName, Department department, String jobName,
            Integer salary, LocalDate entranceDate) {
        this.employeeName = employeeName;
        this.department = department;
        this.jobName = jobName;
        this.salary = salary;
        this.entranceDate = entranceDate;
    }

    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }
    public Integer getSalary() { return salary; }
    public void setSalary(Integer salary) { this.salary = salary; }
    public LocalDate getEntranceDate() { return entranceDate; }
    public void setEntranceDate(LocalDate entranceDate) { this.entranceDate = entranceDate; }
}
