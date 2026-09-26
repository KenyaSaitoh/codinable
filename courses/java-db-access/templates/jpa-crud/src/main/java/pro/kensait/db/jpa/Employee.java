package pro.kensait.db.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;

/*
 * 社員を表すエンティティクラス
 */
@Entity
@Table(name = "EMPLOYEE")
public class Employee {
    @Id
    @Column(name = "EMPLOYEE_ID")
    private Integer id;

    @Column(name = "DEPARTMENT_ID", nullable = false)
    private Integer departmentId;

    @Column(name = "EMPLOYEE_NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "SALARY", nullable = false, precision = 12, scale = 2)
    private BigDecimal salary;

    @Version
    @Column(name = "VERSION")
    private long version;

    // 引数なしのコンストラクタ
    protected Employee() {
    }

    // コンストラクタ
    public Employee(Integer id, Integer departmentId, String name, BigDecimal salary) {
        this.id = id;
        this.departmentId = departmentId;
        this.name = name;
        this.salary = salary;
    }

    // IDの取得
    public Integer getId() {
        return id;
    }

    // 名称の取得
    public String getName() {
        return name;
    }

    // 月給へのアクセサメソッド
    public BigDecimal getSalary() {
        return salary;
    }

    // バージョン（楽観的ロックで使用）へのアクセサメソッド
    public long getVersion() {
        return version;
    }

    // change月給の実行
    public void changeSalary(BigDecimal newSalary) {
        salary = newSalary;
    }
}
