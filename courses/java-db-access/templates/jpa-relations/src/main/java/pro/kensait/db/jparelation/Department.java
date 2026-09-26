package pro.kensait.db.jparelation;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/*
 * 部署を表すエンティティクラス
 */
@Entity
@Table(name = "DEPARTMENT")
public class Department {
    @Id
    private Integer id;
    private String name;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Employee> employees = new ArrayList<>();

    // 引数なしのコンストラクタ
    protected Department() {
    }

    // 部署の初期化
    public Department(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    // 社員の登録
    public void addEmployee(Employee employee) {
        employees.add(employee);
        employee.assignTo(this);
    }

    // IDの取得
    public Integer getId() {
        return id;
    }

    // 名称の取得
    public String getName() {
        return name;
    }

    // 社員のリストへのアクセサメソッド
    public List<Employee> getEmployees() {
        return List.copyOf(employees);
    }
}
