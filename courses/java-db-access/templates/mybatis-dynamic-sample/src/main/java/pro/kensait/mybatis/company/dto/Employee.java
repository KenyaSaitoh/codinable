package pro.kensait.mybatis.company.dto;

import java.time.LocalDate;

import pro.kensait.mybatis.company.type.JobType;

/*
 * 社員を表すエンティティクラス
 */
public class Employee {
    // 社員ID
    private Integer employeeId;
    // 社員名
    private String employeeName;
    // 部署名
    private String departmentName;
    // 入社年月日
    private LocalDate entranceDate;
    // 役職種別
    private JobType jobType;
    // 月給
    private Integer salary;
    // バージョン
    private Long version;

    // 引数なしのコンストラクタ
    public Employee() {
    }

    // コンストラクタ
    public Employee(Integer employeeId, String employeeName, String departmentName,
            LocalDate entranceDate, JobType jobType, Integer salary, Long version) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.departmentName = departmentName;
        this.entranceDate = entranceDate;
        this.jobType = jobType;
        this.salary = salary;
        this.version = version;
    }

    // コンストラクタ
    public Employee(String employeeName, String departmentName, LocalDate entranceDate,
            JobType jobType, Integer salary, Long version) {
        this.employeeName = employeeName;
        this.departmentName = departmentName;
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

    // 部署名へのアクセサメソッド
    public String getDepartmentName() {
        return departmentName;
    }

    // 部署名称の設定
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
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

    // 役職名へのアクセサメソッド
    public JobType getJobType() {
        return jobType;
    }

    // 役職型の設定
    public void setJobType(JobType jobType) {
        this.jobType = jobType;
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
                + ", departmentName=" + departmentName + ", entranceDate=" + entranceDate
                + ", jobType=" + jobType + ", salary=" + salary + ", version=" + version + "]";
    }
}
