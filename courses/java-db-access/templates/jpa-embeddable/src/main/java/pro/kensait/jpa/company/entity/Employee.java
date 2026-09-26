package pro.kensait.jpa.company.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import pro.kensait.jpa.company.type.JobType;

/*
 * 社員を表すエンティティクラス
 */
@Entity
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

    // 役職種別
    @Column(name = "JOB_ID")
    @Enumerated(EnumType.ORDINAL)
    private JobType jobType;

    // 月給
    @Column(name = "SALARY")
    private Integer salary;

    // 住所（エンベッダブルクラス）
    @Embedded
    private Address address; // エンベッダブルクラス

    // 引数なしのコンストラクタ
    public Employee() {
    }

    // コンストラクタ
    public Employee(Integer employeeId, String employeeName,
            Department department, JobType jobType, Integer salary) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
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

    // 住所の取得
    public Address getAddress() {
        return address;
    }

    // 住所の設定
    public void setAddress(Address address) {
        this.address = address;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Employee [employeeId=" + employeeId + ", employeeName=" + employeeName
                + ", department=" + department + ", jobType=" + jobType + ", salary=" + salary
                + ", address=" + address + "]";
    }
}
