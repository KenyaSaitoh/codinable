package pro.kensait.jpa.company.entity;

import jakarta.persistence.AssociationOverride;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

/*
 * sub社員の機能を提供するクラス
 */
@Entity
@Table(name = "EMPLOYEE")
@AssociationOverride(name = "department",
        joinColumns = @JoinColumn(
                name = "DEPARTMENT_ID",
                referencedColumnName = "DEPARTMENT_ID"))
@AttributeOverride(name = "employeeName",
        column = @Column(name = "EMPLOYEE_NAME"))
public class SubEmployee extends SuperEmployee {
    // 月給
    @Column(name = "SALARY")
    private Integer salary;

    // 引数なしのコンストラクタ
    public SubEmployee() {
        super();
    }

    // コンストラクタ
    public SubEmployee(Integer employeeId, String employeeName, Department department,
            Integer salary) {
        super(employeeId, employeeName, department);
        this.salary = salary;
    }

    // アクセサメソッド
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
        return "SubEmployee [salary=" + salary + "]";
    }
}
