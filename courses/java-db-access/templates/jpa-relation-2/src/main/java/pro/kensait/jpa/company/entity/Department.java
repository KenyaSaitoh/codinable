package pro.kensait.jpa.company.entity;

import java.util.List;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MapKey;
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

    // 社員リスト
    @OneToMany(targetEntity = Employee.class, mappedBy = "department")
    private List<Employee> employees;

    // 社員マップ
    @OneToMany(targetEntity = Employee.class, mappedBy = "department")
    @MapKey(name = "employeeId")
    private Map<Integer, Employee> employeeMap;

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

    // 社員mapの取得
    public Map<Integer, Employee> getEmployeeMap() {
        return employeeMap;
    }

    // 社員mapの設定
    public void setEmployeeMap(Map<Integer, Employee> employeeMap) {
        this.employeeMap = employeeMap;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Department [departmentId=" + departmentId + ", departmentName="
                + departmentName + ", location=" + location + "]";
    }
}
