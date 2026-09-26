package pro.kensait.spring.employee.web;

import java.util.Objects;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
/** 登録・更新の確認画面まで入力値を保持するセッションモデル */
public class EmployeeSession {
    private Integer employeeId;

    @NotEmpty
    @Size(max = 30)
    private String employeeName;

    @NotEmpty
    @Size(max = 30)
    private String departmentName;

    @NotNull
    @Min(0)
    @Max(9_999_999)
    private Integer salary;

    // 社員の初期化
    public EmployeeSession() {
    }

    // 社員の初期化
    public EmployeeSession(Integer employeeId, String employeeName, String departmentName,
            Integer salary) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.departmentName = departmentName;
        this.salary = salary;
    }

    // 社員の初期化
    public EmployeeSession(String employeeName, String departmentName, Integer salary) {
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
        if (!(object instanceof EmployeeSession other)) {
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
