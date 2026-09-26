package pro.kensait.jpa.company.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.Table;

/*
 * 社員を表すエンティティクラス
 */
@Entity
@Table(name = "EMPLOYEE")
@SecondaryTable(name = "ADDRESS",
        pkJoinColumns = @PrimaryKeyJoinColumn(name = "ADDRESS_ID"))
public class Employee {
    // 社員IDへのアクセサメソッド
    @Id
    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    // 社員名へのアクセサメソッド
    @Column(name = "EMPLOYEE_NAME")
    private String employeeName;

    // 部署名へのアクセサメソッド
    @Column(name = "DEPARTMENT_NAME")
    private String departmentName;

    // 月給へのアクセサメソッド
    @Column(name = "SALARY")
    private Integer salary;

    // 郵便番号へのアクセサメソッド
    @Column(table = "ADDRESS",
            name = "ZIP_CODE")
    private String zipCode;

    // 都道府県へのアクセサメソッド
    @Column(table = "ADDRESS",
            name = "PREFECTURE")
    private String prefecture;

    // 市町村へのアクセサメソッド
    @Column(table = "ADDRESS",
            name = "CITY")
    private String city;

    // 引数なしのコンストラクタ
    public Employee() {
    }

    // コンストラクタ
    public Employee(Integer employeeId, String employeeName, String departmentName,
            Integer salary, String zipCode, String prefecture, String city) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.departmentName = departmentName;
        this.salary = salary;
        this.zipCode = zipCode;
        this.prefecture = prefecture;
        this.city = city;
    }

    // アクセサメソッド
    public Integer getEmployeeId() {
        return employeeId;
    }

    // 社員IDの設定
    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    // 社員名へのアクセサメソッド
    public String getEmployeeName() {
        return employeeName;
    }

    // 社員名称の設定
    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    // 部署名へのアクセサメソッド
    public String getDepartmentName() {
        return departmentName;
    }

    // 部署名称の設定
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    // 月給へのアクセサメソッド
    public Integer getSalary() {
        return salary;
    }

    // 月給の設定
    public void setSalary(Integer salary) {
        this.salary = salary;
    }


    // アクセサメソッド
    public String getZipCode() {
        return zipCode;
    }

    // ZIPコードの設定
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    // 都道府県の取得
    public String getPrefecture() {
        return prefecture;
    }

    // 都道府県の設定
    public void setPrefecture(String prefecture) {
        this.prefecture = prefecture;
    }

    // 市区町村の取得
    public String getCity() {
        return city;
    }

    // 市区町村の設定
    public void setCity(String city) {
        this.city = city;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Employee [employeeId=" + employeeId + ", employeeName=" + employeeName
                + ", departmentName=" + departmentName + ", salary=" + salary
                + ", zipCode=" + zipCode + ", prefecture=" + prefecture + ", city=" + city
                + "]";
    }
}
