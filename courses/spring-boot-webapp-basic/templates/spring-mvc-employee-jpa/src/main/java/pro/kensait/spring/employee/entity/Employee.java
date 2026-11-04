package pro.kensait.spring.employee.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
/** 社員、部署と役職は @ManyToOne の関連として保持する */
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

    @ManyToOne
    @JoinColumn(name = "DEPARTMENT_ID")
    private Department department;

    @ManyToOne(optional = false)
    @JoinColumn(name = "JOB_ID")
    private Job job;

    @Column(name = "SALARY")
    private Integer salary;

    @Column(name = "ENTRANCE_DATE")
    private LocalDate entranceDate;

    @Column(name = "STATUS")
    private String status = ACTIVE;

    @Version
    @Column(name = "VERSION")
    private Integer version;

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

    // 部署へのアクセサメソッド
    public Department getDepartment() {
        return department;
    }

    // 部署の設定
    public void setDepartment(Department department) {
        this.department = department;
    }

    // 役職へのアクセサメソッド
    public Job getJob() {
        return job;
    }

    // 役職の設定
    public void setJob(Job job) {
        this.job = job;
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

    // version は永続化プロバイダーが管理する。アプリケーションは値を設定しない
    public Integer getVersion() {
        return version;
    }
}
