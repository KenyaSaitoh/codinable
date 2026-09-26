package pro.kensait.spring.employee.rest.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
/** 社員、部署と役職は関連ではなくIDで保持する */
@Entity
@Table(name = "EMPLOYEE")
public class Employee {
    public static final String ACTIVE = "active";
    public static final String DELETED = "deleted";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    @Column(name = "EMPLOYEE_CODE")
    private String employeeCode;

    @Column(name = "EMPLOYEE_NAME")
    private String employeeName;

    @Column(name = "DEPARTMENT_ID")
    private Integer departmentId;

    @Column(name = "JOB_ID")
    private Integer jobId;

    @Column(name = "SALARY")
    private Integer salary;

    @Column(name = "ENTRANCE_DATE")
    private LocalDate entranceDate;

    @Column(name = "STATUS")
    private String status = ACTIVE;

    @Column(name = "VERSION")
    private Integer version = 0;

    // アクセサメソッド
    public Integer getEmployeeId() {
        return employeeId;
    }

    // 社員IDの設定
    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    // 社員コードの取得
    public String getEmployeeCode() {
        return employeeCode;
    }

    // 社員コードの設定
    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    // 社員名へのアクセサメソッド
    public String getEmployeeName() {
        return employeeName;
    }

    // 社員名称の設定
    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    // アクセサメソッド
    public Integer getDepartmentId() {
        return departmentId;
    }

    // 部署IDの設定
    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    // アクセサメソッド
    public Integer getJobId() {
        return jobId;
    }

    // 役職IDの設定
    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    // 月給へのアクセサメソッド
    public Integer getSalary() {
        return salary;
    }

    // 月給の設定
    public void setSalary(Integer salary) {
        this.salary = salary;
    }

    // 入社年月日へのアクセサメソッド
    public LocalDate getEntranceDate() {
        return entranceDate;
    }

    // 入社日付の設定
    public void setEntranceDate(LocalDate entranceDate) {
        this.entranceDate = entranceDate;
    }

    // 状態の取得
    public String getStatus() {
        return status;
    }

    // 状態の設定
    public void setStatus(String status) {
        this.status = status;
    }

    // バージョン（楽観的ロックで使用）へのアクセサメソッド
    public Integer getVersion() {
        return version;
    }

    // バージョンの設定
    public void setVersion(Integer version) {
        this.version = version;
    }
}
