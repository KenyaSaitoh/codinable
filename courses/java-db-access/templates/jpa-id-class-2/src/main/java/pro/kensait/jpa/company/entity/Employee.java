package pro.kensait.jpa.company.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/*
 * 社員を表すエンティティクラス
 */
@Entity
@Table(name = "EMPLOYEE")
@IdClass(value = EmployeePK.class)
public class Employee {
    // 分社ID
    @Id
    @Column(name = "SUBSIDIARY_ID")
    private Integer subsidiaryId;

    // 社員ID
    @Id
    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    // 社員名
    @Column(name = "EMPLOYEE_NAME")
    private String employeeName;

    // 分社
    @ManyToOne(targetEntity = Subsidiary.class)
    @JoinColumn(name = "SUBSIDIARY_ID",
            referencedColumnName = "SUBSIDIARY_ID",
            insertable = false,
            updatable = false)
    private Subsidiary subsidiary;

    // 月給
    @Column(name = "SALARY")
    private Integer salary;

    // 引数なしのコンストラクタ
    public Employee() {
    }

    // コンストラクタ
    public Employee(Integer employeeId, String employeeName, Subsidiary subsidiary,
            Integer salary) {
        this.subsidiaryId = subsidiary.getSubsidiaryId();
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.subsidiary = subsidiary;
        this.salary = salary;
    }

    // アクセサメソッド
    public Integer getSubsidiaryId() {
        return subsidiaryId;
    }

    // subsidiaryIDの設定
    public void setSubsidiaryId(Integer subsidiaryId) {
        this.subsidiaryId = subsidiaryId;
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

    // subsidiaryの取得
    public Subsidiary getSubsidiary() {
        return subsidiary;
    }

    // subsidiaryの設定
    public void setSubsidiary(Subsidiary subsidiary) {
        this.subsidiary = subsidiary;
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
        return "Employee [subsidiaryId=" + subsidiaryId + ", employeeId=" + employeeId
                + ", employeeName=" + employeeName + ", subsidiary=" + (subsidiary == null ? null : subsidiary.getSubsidiaryId())
                + ", salary=" + salary + "]";
    }
}
