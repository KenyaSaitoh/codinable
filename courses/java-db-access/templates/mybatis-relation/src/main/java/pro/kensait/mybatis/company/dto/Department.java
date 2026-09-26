package pro.kensait.mybatis.company.dto;

import java.util.ArrayList;
import java.util.List;

/*
 * 部署を表すエンティティクラス
 */
public class Department {
    private Integer departmentId;
    private String departmentName;
    private String location;
    private List<Employee> employees = new ArrayList<Employee>();

    // 引数なしのコンストラクタ
    public Department() {
    }

    // コンストラクタ
    public Department(Integer departmentId, String departmentName,
            String location, List<Employee> employees) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.location = location;
        this.employees = employees;
    }

    // 部署IDへのアクセサメソッド
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
}
