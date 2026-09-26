package pro.kensait.spring.employee.rest.entity;
/*
 * 社員検索条件
 */
public class EmployeeSearchCriteria {
    private String keyword;
    private Integer departmentId;
    private Integer jobId;
    private Integer salaryFrom;
    private Integer salaryTo;

    // 条件を指定しない検索条件の生成
    public static EmployeeSearchCriteria empty() {
        return new EmployeeSearchCriteria();
    }

    // 未入力のキーワードを検索条件から除外
    public EmployeeSearchCriteria normalized() {
        EmployeeSearchCriteria criteria = new EmployeeSearchCriteria();
        criteria.keyword = keyword == null || keyword.isBlank() ? null : keyword.strip();
        criteria.departmentId = departmentId;
        criteria.jobId = jobId;
        criteria.salaryFrom = salaryFrom;
        criteria.salaryTo = salaryTo;
        return criteria;
    }

    // 月給の下限が上限を超えているか確認
    public boolean rangeReversed() {
        return salaryFrom != null && salaryTo != null && salaryFrom > salaryTo;
    }

    // キーワードの取得
    public String getKeyword() {
        return keyword;
    }

    // キーワードの設定
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    // アクセサメソッド
    public Integer getDepartmentId() {
        return departmentId;
    }

    // 部署IDの設定
    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    // アクセサメソッド
    public Integer getJobId() {
        return jobId;
    }

    // 役職IDの設定
    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    // 月給変換元の取得
    public Integer getSalaryFrom() {
        return salaryFrom;
    }

    // 月給変換元の設定
    public void setSalaryFrom(Integer salaryFrom) {
        this.salaryFrom = salaryFrom;
    }

    // 月給変換先の取得
    public Integer getSalaryTo() {
        return salaryTo;
    }

    // 月給変換先の設定
    public void setSalaryTo(Integer salaryTo) {
        this.salaryTo = salaryTo;
    }
}
