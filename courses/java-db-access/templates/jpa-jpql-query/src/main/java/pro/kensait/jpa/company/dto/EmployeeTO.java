package pro.kensait.jpa.company.dto;

/*
 * 社員変換先の機能を提供するクラス
 */
public class EmployeeTO {
    // 社員ID
    private Integer employeeId;

    // 社員名
    private String employeeName;

    // 部署名
    private String departmentName;

    // 引数なしのコンストラクタ
    public EmployeeTO() {
    }

    // コンストラクタ
    public EmployeeTO(Integer employeeId, String employeeName, String departmentName) {
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
        return "EmployeeTO [" + employeeId + ", " + employeeName + ", "
                + departmentName + "]";
    }
}
