package pro.kensait.jpa.company.entity;

import java.time.LocalDate;
import java.util.Arrays;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import pro.kensait.jpa.company.type.JobType;

/*
 * 社員を表すエンティティクラス
 */
@Entity
@Table(name = "EMPLOYEE")
public class Employee {
    @Id
    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    @Column(name = "EMPLOYEE_NAME")
    private String employeeName;

    @Column(name = "DEPARTMENT_NAME")
    private String departmentName;

    @Column(name = "ENTRANCE_DATE")
    private LocalDate entranceDate;

    @Column(name = "JOB_NAME")
    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @Column(name = "SALARY")
    private Integer salary;

    @Column(name = "PHOTO")
    @Lob
    private byte[] photo;

    // 引数なしのコンストラクタ
    public Employee() {
    }

    // コンストラクタ
    public Employee(Integer employeeId, String employeeName,
            String departmentName, LocalDate entranceDate, JobType jobType,
            Integer salary) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.departmentName = departmentName;
        this.entranceDate = entranceDate;
        this.jobType = jobType;
        this.salary = salary;
    }

    // 社員IDへのアクセサメソッド
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

    // 写真へのアクセサメソッド
    public byte[] getPhoto() {
        return photo;
    }

    // photoの設定
    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

	// 文字列表現の生成
 @Override
	public String toString() {
		return "Employee [employeeId=" + employeeId + ", employeeName=" + employeeName + ", departmentName="
				+ departmentName + ", entranceDate=" + entranceDate + ", jobType=" + jobType + ", salary=" + salary
				+ ", photo=" + Arrays.toString(photo) + "]";
	}
}
