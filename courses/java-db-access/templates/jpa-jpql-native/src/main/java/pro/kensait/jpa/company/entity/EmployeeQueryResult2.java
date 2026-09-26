package pro.kensait.jpa.company.entity;

import jakarta.persistence.ColumnResult;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityResult;
import jakarta.persistence.FieldResult;
import jakarta.persistence.Id;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.SqlResultSetMappings;

/*
 * 社員クエリ結果2の機能を提供するクラス
 */
@Entity
@SqlResultSetMappings(value = {
@SqlResultSetMapping(name = "NativeQueryResult1",
        entities = {
            @EntityResult(
                    entityClass = EmployeeQueryResult2.class,
                    fields = {
                        @FieldResult(name = "employeeId",
                                column = "E_EMPLOYEE_ID"),
                        @FieldResult(name = "employeeName",
                                column = "E_EMPLOYEE_NAME"),
                        @FieldResult(name = "departmentName",
                                column = "D_DEPARTMENT_NAME")
                    }
            )
        }
),
@SqlResultSetMapping(name = "NativeQueryResult2",
        columns = {
            @ColumnResult(name = "E_SALARY"),
            @ColumnResult(name = "D_LOCATION")
        }
),
@SqlResultSetMapping(name = "NativeQueryResult3",
        entities = {
            @EntityResult(
                    entityClass = EmployeeQueryResult2.class,
                    fields = {
                        @FieldResult(name = "employeeId",
                                column = "E_EMPLOYEE_ID"),
                        @FieldResult(name = "employeeName",
                                column = "E_EMPLOYEE_NAME"),
                        @FieldResult(name = "departmentName",
                                column = "D_DEPARTMENT_NAME")
                    }
            )
        },
        columns = {
            @ColumnResult(name = "E_SALARY"),
            @ColumnResult(name = "D_LOCATION")
        }
)
})
@NamedNativeQuery(name = "findEmployeesByDepartmentId2",
        query = "SELECT e.EMPLOYEE_ID AS E_EMPLOYEE_ID, " +
                "e.EMPLOYEE_NAME AS E_EMPLOYEE_NAME, " +
                "d.DEPARTMENT_NAME AS D_DEPARTMENT_NAME, " +
                "e.SALARY AS E_SALARY, " +
                "d.LOCATION AS D_LOCATION " +
                "FROM EMPLOYEE e, DEPARTMENT d " +
                "WHERE e.DEPARTMENT_ID = d.DEPARTMENT_ID " +
                "AND e.EMPLOYEE_ID = ?1",
        resultSetMapping = "NativeQueryResult1")
public class EmployeeQueryResult2 {
    // 社員ID
    @Id
    private Integer employeeId;

    // 社員名
    private String employeeName;

    // 部署名
    private String departmentName;

    // 引数なしのコンストラクタ
    public EmployeeQueryResult2() {
    }

    // コンストラクタ
    public EmployeeQueryResult2(Integer employeeId, String employeeName,
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
        return "EmployeeQueryResult2 [" + employeeId + ", " + employeeName
                + ", " + departmentName + "]";
    }
}
