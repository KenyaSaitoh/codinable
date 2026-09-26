package pro.kensait.jpa.company.dto;

/*
 * empdeptの機能を提供するクラス
 */
public class EmpDept {
    // 社員ID
    private Integer employeeId;

    // 社員名
    private String employeeName;

    // 月給
    private Integer salary;

    // 部署ID
    private Integer departmentId;

    // 部署名
    private String departmentName;

    // 所在地
    private String location;

    // 引数なしのコンストラクタ
    public EmpDept() {
    }

    // コンストラクタ
    public EmpDept(Integer employeeId, String employeeName, Integer salary,
            Integer departmentId, String departmentName, String location) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.salary = salary;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.location = location;
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

    // 月給へのアクセサメソッド
    public Integer getSalary() {
        return salary;
    }

    // 月給の設定
    public void setSalary(Integer salary) {
        this.salary = salary;
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
        return "EmployeeDepartment [" + employeeId + ", " + employeeName + ", "
                + salary + ", " + departmentId + ", " + departmentName + ", "
                + location + "]";
    }
}
