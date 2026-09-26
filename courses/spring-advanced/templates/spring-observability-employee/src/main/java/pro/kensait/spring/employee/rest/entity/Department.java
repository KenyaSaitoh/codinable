package pro.kensait.spring.employee.rest.entity;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

    // 引数のないコンストラクタ
    public Department() {}

    // コンストラクタ
    public Department(Integer departmentId, String departmentName, String location) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.location = location;
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

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Department [departmentId=" + departmentId + ", departmentName="
                + departmentName + ", location=" + location + "]";
    }

    // equalsメソッドに合わせて、hashcodeメソッドもオーバーライド
    @Override
    public int hashCode() {
        return Objects.hash(departmentId, departmentName, location);
    }

    // 一意性を保証するために、必ずequalsメソッドをオーバーライド
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Department other = (Department) obj;
        return Objects.equals(departmentId, other.departmentId)
                && Objects.equals(departmentName, other.departmentName)
                && Objects.equals(location, other.location);
    }
}
