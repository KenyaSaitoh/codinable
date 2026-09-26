package pro.kensait.db.mybatis;

import java.math.BigDecimal;

/*
 * 社員を表すエンティティクラス
 */
public class Employee {
    private Integer id;
    private Integer departmentId;
    private String name;
    private BigDecimal salary;

    // 引数なしのコンストラクタ
    public Employee() {
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

    // IDの設定
    public void setId(Integer value) {
        id = value;
    }

    // アクセサメソッド
    public Integer getDepartmentId() {
        return departmentId;
    }

    // 部署IDの設定
    public void setDepartmentId(Integer value) {
        departmentId = value;
    }

    // 名称の取得
    public String getName() {
        return name;
    }

    // 名称の設定
    public void setName(String value) {
        name = value;
    }

    // 月給へのアクセサメソッド
    public BigDecimal getSalary() {
        return salary;
    }

    // 月給の設定
    public void setSalary(BigDecimal value) {
        salary = value;
    }
}
