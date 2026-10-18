package pro.kensait.jakartaee.employee.entity;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * EMPLOYEE テーブルの 1 行を保持する JavaBean（Jakarta Persistence のエンティティではない）
 * 部署と役職はオブジェクトの関連ではなく、ID で保持する
 */
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String ACTIVE = "active";
    public static final String DELETED = "deleted";

    private Integer employeeId;
    private String employeeCode;
    private String employeeName;
    private Integer departmentId;
    private Integer jobId;
    private Integer salary;
    private LocalDate entranceDate;
    private String status = ACTIVE;
    private Integer version = 0;

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
