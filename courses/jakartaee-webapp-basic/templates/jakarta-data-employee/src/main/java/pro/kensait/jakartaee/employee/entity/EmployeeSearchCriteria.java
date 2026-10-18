package pro.kensait.jakartaee.employee.entity;

import java.io.Serializable;

/** 社員の検索条件（キーワード・部署・役職・月給の下限と上限）。null の項目は条件にしない */
public class EmployeeSearchCriteria implements Serializable {
    private static final long serialVersionUID = 1L;

    private String keyword;
    private Integer departmentId;
    private Integer jobId;
    private Integer salaryFrom;
    private Integer salaryTo;

    // 未入力のキーワードを検索条件から除き、前後の空白を取る
    public EmployeeSearchCriteria normalized() {
        EmployeeSearchCriteria criteria = new EmployeeSearchCriteria();
        criteria.keyword = keyword == null || keyword.isBlank() ? null : keyword.strip();
        criteria.departmentId = departmentId;
        criteria.jobId = jobId;
        criteria.salaryFrom = salaryFrom;
        criteria.salaryTo = salaryTo;
        return criteria;
    }

    // 月給の下限が上限を超えているか
    public boolean rangeReversed() {
        return salaryFrom != null && salaryTo != null && salaryFrom > salaryTo;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public Integer getSalaryFrom() {
        return salaryFrom;
    }

    public void setSalaryFrom(Integer salaryFrom) {
        this.salaryFrom = salaryFrom;
    }

    public Integer getSalaryTo() {
        return salaryTo;
    }

    public void setSalaryTo(Integer salaryTo) {
        this.salaryTo = salaryTo;
    }
}
