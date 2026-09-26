package pro.kensait.spring.employee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
/** 部署マスタ */
@Entity
@Table(name = "DEPARTMENT")
public class Department {
    @Id
    @Column(name = "DEPARTMENT_ID")
    private Integer departmentId;

    @Column(name = "DEPARTMENT_NAME")
    private String departmentName;

    @Column(name = "LOCATION")
    private String location;

    // アクセサメソッド
    public Integer getDepartmentId() {
        return departmentId;
    }

    // 部署名へのアクセサメソッド
    public String getDepartmentName() {
        return departmentName;
    }

    // 所在地へのアクセサメソッド
    public String getLocation() {
        return location;
    }
}
