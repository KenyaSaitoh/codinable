package pro.kensait.jpa.company.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/*
 * 社員を表すエンティティクラス
 */
@Entity
@Table(name = "EMPLOYEE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "EMPLOYEE_TYPE",
        discriminatorType = DiscriminatorType.STRING)
public abstract class Employee {
    // 社員ID
    @Id
    @Column(name = "EMPLOYEE_ID")
    protected Integer employeeId;

    // 社員名
    @Column(name = "EMPLOYEE_NAME")
    protected String employeeName;

    // 部署
    @ManyToOne(targetEntity = Department.class)
    @JoinColumn(name = "DEPARTMENT_ID",
            referencedColumnName = "DEPARTMENT_ID")
    protected Department department;

    // 社員種別
    /*
     * 社員種別は生成されるインスタンスから判別可能なので、なくても問題ない
     * あえて定義する場合は「insertable=false, updatable=false」が必要
     */
    @Column(name = "EMPLOYEE_TYPE",
            insertable=false,
            updatable=false)
    protected Integer employeeType;

    // 入社年月日
    @Column(name = "ENTRANCE_DATE")
    @Temporal(value = TemporalType.DATE)
    protected LocalDate entranceDate;

    // 引数なしのコンストラクタ
    public Employee() {
    }

    // コンストラクタ
    public Employee(Integer employeeId, String employeeName, Department department,
            Integer employeeType, LocalDate entranceDate) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
        this.employeeType = employeeType;
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

    // 社員型の取得
    public Integer getEmployeeType() {
        return employeeType;
    }

    // 社員型の設定
    public void setEmployeeType(Integer employeeType) {
        this.employeeType = employeeType;
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
                + ", department=" + department + ", employeeType=" + employeeType
                + ", entranceDate=" + entranceDate + "]";
    }
}
