package pro.kensait.jpa.company.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedNativeQuery;

/*
 * 社員クエリ結果1の機能を提供するクラス
 */
@Entity
@NamedNativeQuery(name = "findEmployeesByDepartmentId1",
        query = "SELECT e.EMPLOYEE_ID AS E_EMPLOYEE_ID, " +
                "e.EMPLOYEE_NAME AS E_EMPLOYEE_NAME, " +
                "d.DEPARTMENT_NAME AS D_DEPARTMENT_NAME " +
                "FROM EMPLOYEE e, DEPARTMENT d " +
                "WHERE e.DEPARTMENT_ID = d.DEPARTMENT_ID " +
                "AND e.EMPLOYEE_ID = ?1",
        resultClass = EmployeeQueryResult1.class)
public class EmployeeQueryResult1 {
    // 社員ID
    @Id
    @Column(name = "E_EMPLOYEE_ID")
    private Integer employeeId;

    // 社員名
    @Column(name = "E_EMPLOYEE_NAME")
    private String employeeName;

    // 部署名
    @Column(name = "D_DEPARTMENT_NAME")
    private String departmentName;

    // 引数なしのコンストラクタ
    public EmployeeQueryResult1() {
    }

    // コンストラクタ
    public EmployeeQueryResult1(Integer employeeId, String employeeName,
            String departmentName) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.departmentName = departmentName;
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

    // 文字列表現の生成
    @Override
    public String toString() {
        return "EmployeeQueryResult1 [" + employeeId + ", " + employeeName
                + ", " + departmentName + "]";
    }
}
