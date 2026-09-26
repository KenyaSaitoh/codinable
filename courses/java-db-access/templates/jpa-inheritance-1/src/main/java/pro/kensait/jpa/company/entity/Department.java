package pro.kensait.jpa.company.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/*
 * 部署を表すエンティティクラス
 */
@Entity
@Table(name = "DEPARTMENT")
public class Department {
    // 部署ID
    @Id
    @Column(name = "DEPARTMENT_ID")
    private Integer departmentId;

    // 部署名
    @Column(name = "DEPARTMENT_NAME")
    private String departmentName;

    // 所在地
    @Column(name = "LOCATION")
    private String location;

    // 社員のリスト
    @OneToMany(targetEntity = Employee.class,
            mappedBy = "department",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<Employee> employees = new ArrayList<Employee>();

    // 引数なしのコンストラクタ
    public Department() {
    }

    // コンストラクタ
    public Department(Integer departmentId, String departmentName, String location,
            List<Employee> employees) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.location = location;
        this.employees = employees;
    }

    // アクセサメソッド
    public Integer getDepartmentId() {
        return departmentId;
    }

    // 部署IDの設定
    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    // 部署名へのアクセサメソッド
    public String getDepartmentName() {
        return departmentName;
    }

    // 部署名称の設定
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    // 所在地へのアクセサメソッド
    public String getLocation() {
        return location;
    }

    // 所在地の設定
    public void setLocation(String location) {
        this.location = location;
    }

    // 社員のリストへのアクセサメソッド
    public List<Employee> getEmployees() {
        return employees;
    }

    // 社員の設定
    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Department [departmentId=" + departmentId + ", departmentName="
                + departmentName + ", location=" + location + "]";
    }
}
