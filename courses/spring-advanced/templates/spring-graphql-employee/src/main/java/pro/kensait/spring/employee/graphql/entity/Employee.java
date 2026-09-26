package pro.kensait.spring.employee.graphql.entity;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/*
 * 社員を表すエンティティクラス
 */
@Entity
@Table(name = "EMPLOYEE")
public class Employee {
    // 社員ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    // 社員名
    @Column(name = "EMPLOYEE_NAME")
    private String employeeName;

    // 部署（EMPLOYEEテーブルのDEPARTMENT_IDカラムで結合する）
    @ManyToOne
    @JoinColumn(name = "DEPARTMENT_ID")
    private Department department;

    // 職種
    @Column(name = "JOB_NAME")
    private String jobName;

    // 給与
    @Column(name = "SALARY")
    private Integer salary;

    // 入社日
    @Column(name = "ENTRANCE_DATE")
    private LocalDate entranceDate;

    // 引数のないコンストラクタ
    public Employee() {}

    // コンストラクタ
    public Employee(Integer employeeId, String employeeName, Department department,
            String jobName, Integer salary, LocalDate entranceDate) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
        this.jobName = jobName;
        this.salary = salary;
        this.entranceDate = entranceDate;
    }

    // コンストラクタ
    public Employee(String employeeName, Department department, String jobName,
            Integer salary, LocalDate entranceDate) {
        this.employeeName = employeeName;
        this.department = department;
        this.jobName = jobName;
        this.salary = salary;
        this.entranceDate = entranceDate;
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

    // アクセサメソッド
    public String getJobName() {
        return jobName;
    }

    // 役職名称の設定
    public void setJobName(String jobName) {
        this.jobName = jobName;
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

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Employee [employeeId=" + employeeId + ", employeeName=" + employeeName
                + ", department=" + department + ", jobName=" + jobName + ", salary="
                + salary + ", entranceDate=" + entranceDate + "]";
    }

    // equalsメソッドに合わせて、hashcodeメソッドもオーバーライド
    @Override
    public int hashCode() {
        return Objects.hash(department, employeeId, employeeName, entranceDate, jobName,
                salary);
    }

    // 一意性を保証するために、必ずequalsメソッドをオーバーライド
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Employee other = (Employee) obj;
        return Objects.equals(department, other.department)
                && Objects.equals(employeeId, other.employeeId)
                && Objects.equals(employeeName, other.employeeName)
                && Objects.equals(entranceDate, other.entranceDate)
                && Objects.equals(jobName, other.jobName)
                && Objects.equals(salary, other.salary);
    }
}
