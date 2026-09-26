package pro.kensait.jpa.company.dto;

/*
 * 社員件数変換先の機能を提供するクラス
 */
public class EmployeeCountTO {
    // 部署名
    private Integer departmentId;

    // 所属社員数
    private Long employeeCount;

    // 引数なしのコンストラクタ
    public EmployeeCountTO() {
    }

    // コンストラクタ
    public EmployeeCountTO(Integer departmentId, Long employeeCount) {
        this.departmentId = departmentId;
        this.employeeCount = employeeCount;
    }

    // アクセサメソッド
    public Integer getDepartmentId() {
        return departmentId;
    }

    // 部署IDの設定
    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    // 社員件数の取得
    public Long getEmployeeCount() {
        return employeeCount;
    }

    // 社員件数の設定
    public void setEmployeeCount(Long employeeCount) {
        this.employeeCount = employeeCount;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "EmployeeCountTO [" + departmentId + ", " + employeeCount + "]";
    }
}
