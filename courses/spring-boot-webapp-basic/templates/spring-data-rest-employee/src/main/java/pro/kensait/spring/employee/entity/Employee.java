package pro.kensait.spring.employee.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.jdbc.Expectation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
/** 社員、部署と役職は関連ではなくIDで保持する */
@Entity
@Table(name = "EMPLOYEE")
// 第10章の論理削除を、Repositoryの標準delete/find系メソッドでも維持する
@SQLDelete(sql = "UPDATE EMPLOYEE SET STATUS = 'deleted', VERSION = VERSION + 1 "
        + "WHERE EMPLOYEE_ID = ? AND VERSION = ?", verify = Expectation.RowCount.class)
@SQLRestriction("STATUS = 'active'")
public class Employee {
    public static final String ACTIVE = "active";
    public static final String DELETED = "deleted";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMPLOYEE_ID")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer employeeId;

    @Column(name = "EMPLOYEE_CODE")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String employeeCode = "E0000";

    @NotBlank
    @Size(max = 30)
    @Column(name = "EMPLOYEE_NAME")
    private String employeeName;

    @NotNull
    @Positive
    @Column(name = "DEPARTMENT_ID")
    private Integer departmentId;

    @NotNull
    @Positive
    @Column(name = "JOB_ID")
    private Integer jobId;

    @NotNull
    @Min(0)
    @Max(9_999_999)
    @Column(name = "SALARY")
    private Integer salary;

    @NotNull
    @Column(name = "ENTRANCE_DATE")
    private LocalDate entranceDate;

    @Column(name = "STATUS")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String status = ACTIVE;

    @Version
    @Column(name = "VERSION")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer version;

    // IDENTITY採番後、同じ保存トランザクションのdirty checkingで社員コードを確定
    @PostPersist
    private void assignEmployeeCode() {
        employeeCode = "E%04d".formatted(employeeId);
    }

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
