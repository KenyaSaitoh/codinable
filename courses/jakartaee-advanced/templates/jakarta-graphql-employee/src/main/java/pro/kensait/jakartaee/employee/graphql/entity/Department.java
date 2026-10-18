package pro.kensait.jakartaee.employee.graphql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.NonNull;

/*
 * 部署のエンティティ。GraphQL の Department 型にもなる
 * 所属社員の一覧（employees）はエンティティには無く、EmployeeGraphQLApi の @Source のメソッドが加える
 */
@Entity
@Table(name = "DEPARTMENT")
public class Department {
    @jakarta.persistence.Id
    @Column(name = "DEPARTMENT_ID")
    @Name("id") @Id @NonNull
    private Integer departmentId;
    @Column(name = "DEPARTMENT_NAME")
    @NonNull
    private String departmentName;
    @Column(name = "LOCATION")
    private String location;

    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
