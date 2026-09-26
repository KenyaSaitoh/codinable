package pro.kensait.jpa.company.entity;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

/*
 * 社員を表すエンティティクラス
 */
@Entity
@Table(name = "EMPLOYEE")
public class Employee {
    private Integer employeeId;
    private String employeeName;
    private String departmentName;
    private Integer salary;
    private List<String> emailList;

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

    // メールアドレス一覧の取得
    @ElementCollection
    @CollectionTable(name = "EMAIL",
            joinColumns = @JoinColumn(name = "EMPLOYEE_ID"))
    @Column(name = "ADDRESS")
    public List<String> getEmailList() {
        return emailList;
    }

    // メールアドレス一覧の設定
    public void setEmailList(List<String> emailList) {
        this.emailList = emailList;
    }

}
