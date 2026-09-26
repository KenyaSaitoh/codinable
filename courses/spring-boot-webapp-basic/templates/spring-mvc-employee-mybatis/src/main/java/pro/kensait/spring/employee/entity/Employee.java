package pro.kensait.spring.employee.entity;

import java.time.LocalDate;
/** 社員、部署と役職はIDで保持する */
public class Employee {
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
