package pro.kensait.jpa.company.entity;

import java.time.LocalDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import pro.kensait.jpa.company.type.JobType;

/*
 * 社員を表すエンティティクラス
 */
@Entity
@NamedQuery(name = "findEmployeesBysalary",
        query = "select e from Employee e where e.salary >= :salary order by e.employeeId")
@Table(name = "EMPLOYEE")
public class Employee {
    // 社員ID
    @Id
    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    // 社員名
    @Column(name = "EMPLOYEE_NAME")
    private String employeeName;

    // 部署
    @ManyToOne(targetEntity = Department.class,
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    @JoinColumn(name = "DEPARTMENT_ID",
            referencedColumnName = "DEPARTMENT_ID")
    private Department department;

    // 入社年月日
    @Column(name = "ENTRANCE_DATE")
    private LocalDate entranceDate;

    // 役職ID
    @Column(name = "JOB_ID")
    @Enumerated(EnumType.ORDINAL)
    private JobType jobType;

    // 月給
    @Column(name = "SALARY")
    private Integer salary;

    // バージョン（楽観的ロックで使用）
    @Version
    private Long version;

    // 引数なしのコンストラクタ
    public Employee() {
    }

    // コンストラクタ
    public Employee(Integer employeeId, String employeeName, Department department,
            LocalDate entranceDate, JobType jobType, Integer salary, Long version) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
        this.entranceDate = entranceDate;
        this.jobType = jobType;
        this.salary = salary;
        this.version = version;
    }

    // アクセサメソッド
    public Integer getEmployeeId() {
        return employeeId;
    }

    // 社員IDの設定
    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
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

    // 入社年月日へのアクセサメソッド
    public LocalDate getEntranceDate() {
        return entranceDate;
    }

    // 入社日付の設定
    public void setEntranceDate(LocalDate entranceDate) {
        this.entranceDate = entranceDate;
    }

    // 役職名へのアクセサメソッド
    public JobType getJobType() {
        return jobType;
    }

    // 役職型の設定
    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    // 月給へのアクセサメソッド
    public Integer getSalary() {
        return salary;
    }

    // 月給の設定
    public void setSalary(Integer salary) {
        this.salary = salary;
    }

    // バージョン（楽観的ロックで使用）へのアクセサメソッド
    public Long getVersion() {
        return version;
    }

    // バージョンの設定
    public void setVersion(Long version) {
        this.version = version;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Employee [employeeId=" + employeeId + ", employeeName=" + employeeName
                + ", department=" + department + ", entranceDate=" + entranceDate + ", jobType="
                + jobType + ", salary=" + salary + ", version=" + version + "]";
    }
}
