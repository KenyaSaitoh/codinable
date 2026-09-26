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
    // 部署
    private Department department;
    // 入社年月日
    private LocalDate entranceDate;
    // 役職種別
    private JobType jobType;
    // 月給
    private Integer salary;

    // 引数なしのコンストラクタ
    public Employee() {
    }

    // コンストラクタ
    public Employee(Integer employeeId, String employeeName,
            Department department, LocalDate entranceDate, JobType jobType,
            Integer salary) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
        this.entranceDate = entranceDate;
        this.jobType = jobType;
        this.salary = salary;
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

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Employee [employeeId=" + employeeId + ", employeeName=" + employeeName
                + ", department=" + department + ", entranceDate=" + entranceDate + ", jobType="
                + jobType + ", salary=" + salary + "]";
    }
}
