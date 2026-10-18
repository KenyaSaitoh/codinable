package pro.kensait.jakartaee.employee.rest;

import jakarta.ws.rs.QueryParam;
import pro.kensait.jakartaee.employee.entity.EmployeeSearchCriteria;

/** 一覧の検索条件（@BeanParam でクエリ文字列からまとめて受け取る） */
public class EmployeeSearchParam {
    @QueryParam("keyword")
    private String keyword;

    @QueryParam("departmentId")
    private Integer departmentId;

    @QueryParam("jobId")
    private Integer jobId;

    @QueryParam("salaryFrom")
    private Integer salaryFrom;

    @QueryParam("salaryTo")
    private Integer salaryTo;

    public EmployeeSearchCriteria toCriteria() {
        EmployeeSearchCriteria criteria = new EmployeeSearchCriteria();
        criteria.setKeyword(keyword);
        criteria.setDepartmentId(departmentId);
        criteria.setJobId(jobId);
        criteria.setSalaryFrom(salaryFrom);
        criteria.setSalaryTo(salaryTo);
        return criteria.normalized();
    }
}
