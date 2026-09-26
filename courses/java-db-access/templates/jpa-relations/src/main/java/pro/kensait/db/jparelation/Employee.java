package pro.kensait.db.jparelation;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/*
 * 社員を表すエンティティクラス
 */
@Entity
@Table(name = "EMPLOYEE")
public class Employee {
    @Id
    private Integer id;
    private String name;
    private BigDecimal salary;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DEPARTMENT_ID")
    private Department department;

    // 引数なしのコンストラクタ
    protected Employee() {
    }

    // コンストラクタ
    public Employee(Integer id, String name, BigDecimal salary) {
        this.id = id;
        this.name = name;
        this.salary = salary;
    }

    // assign変換先の実行
    void assignTo(Department value) {
        department = value;
    }

    // IDの取得
    public Integer getId() {
        return id;
    }

    // 月給へのアクセサメソッド
    public BigDecimal getSalary() {
        return salary;
    }

}
