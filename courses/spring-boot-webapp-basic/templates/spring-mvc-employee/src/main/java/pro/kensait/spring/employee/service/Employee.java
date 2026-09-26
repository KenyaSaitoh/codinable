package pro.kensait.spring.employee.service;

import java.util.Objects;
/** 部署名を文字列で保持する、関連を持たない社員モデル */
public class Employee {
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

    // コンストラクタ
    public Employee(String employeeName, String departmentName, Integer salary) {
        this(null, employeeName, departmentName, salary);
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

    // 一意性を保証するために、必ずequalsメソッドをオーバーライド
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Employee other)) {
            return false;
        }
        return Objects.equals(employeeId, other.employeeId)
                && Objects.equals(employeeName, other.employeeName)
                && Objects.equals(departmentName, other.departmentName)
                && Objects.equals(salary, other.salary);
    }

    // equalsメソッドに合わせて、hashcodeメソッドもオーバーライド
    @Override
    public int hashCode() {
        return Objects.hash(employeeId, employeeName, departmentName, salary);
    }
}
