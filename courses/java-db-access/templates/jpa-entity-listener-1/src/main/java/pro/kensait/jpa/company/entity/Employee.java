package pro.kensait.jpa.company.entity;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/*
 * 社員を表すエンティティクラス
 */
@Entity
@Table(name = "EMPLOYEE")
@EntityListeners({EmployeeEntityListener.class})
public class Employee implements Serializable {
    private Integer employeeId;
    private String employeeName;
    private String departmentName;
    private Integer salary;

    // 引数なしのコンストラクタ
    public Employee() {
    }

    // コンストラクタ
    public Employee(Integer employeeId, String employeeName, String departmentName,
            Integer salary) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.departmentName = departmentName;
        this.salary = salary;
    }

    // 社員IDへのアクセサメソッド
    @Id
    @Column(name = "EMPLOYEE_ID")
    public Integer getEmployeeId() {
        return employeeId;
    }

    // 社員IDの設定
    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    // 社員名へのアクセサメソッド
    @Column(name = "EMPLOYEE_NAME")
    public String getEmployeeName() {
        return employeeName;
    }

    // 社員名称の設定
    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    // 部署名へのアクセサメソッド
    @Column(name = "DEPARTMENT_NAME")
    public String getDepartmentName() {
        return departmentName;
    }

    // 部署名称の設定
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    // 月給へのアクセサメソッド
    @Column(name = "SALARY")
    public Integer getSalary() {
        return salary;
    }

    // 月給の設定
    public void setSalary(Integer salary) {
        this.salary = salary;
    }

    // コールバックメソッド（SELECT文の発行後に呼び出される）
    @PostLoad
    public void postLoad() {
        System.out.println("[ Employee#postLoad ]");
    }

    // コールバックメソッド（INSERT文の発行前に呼び出される）
    @PrePersist
    public void prePersist() {
        System.out.println("[ Employee#prePersist ]");
    }

    // コールバックメソッド（INSERT文の発行後に呼び出される）
    @PostPersist
    public void postPersist() {
        System.out.println("[ Employee#postPersist ]");
    }

    // コールバックメソッド（DELETE文の発行前に呼び出される）
    @PreRemove
    public void preRemove() {
        System.out.println("[ Employee#preRemove ]");
    }

    // コールバックメソッド（DELETE文の発行後に呼び出される）
    @PostRemove
    public void postRemove() {
        System.out.println("[ Employee#postRemove ]");
    }

    // コールバックメソッド（UPDATE文の発行前に呼び出される）
    @PreUpdate
    public void preUpdate() {
        System.out.println("[ Employee#preUpdate ]");
    }

    // コールバックメソッド（UPDATE文の発行後に呼び出される）
    @PostUpdate
    public void postUpdate() {
        System.out.println("[ Employee#postUpdate ]");
    }
}
