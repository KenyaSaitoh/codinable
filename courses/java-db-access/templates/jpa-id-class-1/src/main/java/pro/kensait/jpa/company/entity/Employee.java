package pro.kensait.jpa.company.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/*
 * 社員を表すエンティティクラス
 */
@Entity
@Table(name = "EMPLOYEE")
@IdClass(value = EmployeePK.class)
public class Employee {
    // 分社名
    @Id
    @Column(name = "SUBSIDIARY_NAME")
    private String subsidiaryName;

    // 社員ID
    @Id
    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    // 社員名
    @Column(name = "EMPLOYEE_NAME")
    private String employeeName;

    // 月給
    @Column(name = "SALARY")
    private Integer salary;

    // 引数なしのコンストラクタ
    public Employee() {
    }

    // コンストラクタ
    public Employee(String subsidiaryName,Integer employeeId, String employeeName,
            Integer salary) {
        this.subsidiaryName = subsidiaryName;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.salary = salary;
    }

    // アクセサメソッド
    public String getSubsidiaryName() {
        return subsidiaryName;
    }

    // subsidiary名称の設定
    public void setSubsidiaryName(String subsidiaryName) {
        this.subsidiaryName = subsidiaryName;
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
        return "Employee [subsidiaryName=" + subsidiaryName + ", employeeId=" + employeeId
                + ", employeeName=" + employeeName + ", salary=" + salary + "]";
    }
}
