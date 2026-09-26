package pro.kensait.jpa.company.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

/*
 * super社員の機能を提供するクラス
 */
@MappedSuperclass
public class SuperEmployee {
    // 社員IDへのアクセサメソッド
    @Id
    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    // 社員名へのアクセサメソッド
    @Column(name = "EMPLOYEE_NAME")
    private String employeeName;

    // 部署へのアクセサメソッド
    @ManyToOne(targetEntity = Department.class)
    private Department department;

    // 引数なしのコンストラクタ
    public SuperEmployee() {
    }

    // コンストラクタ
    public SuperEmployee(Integer employeeId, String employeeName,
            Department department) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
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

    // 部署へのアクセサメソッド
    public Department getDepartment() {
        return department;
    }

    // 部署の設定
    public void setDepartment(Department department) {
        this.department = department;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "SuperEmployee [employeeId=" + employeeId + ", employeeName="
                + employeeName + ", department=" + department + "]";
    }
}
