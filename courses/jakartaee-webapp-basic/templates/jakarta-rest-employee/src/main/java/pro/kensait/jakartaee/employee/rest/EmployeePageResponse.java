package pro.kensait.jakartaee.employee.rest;

import java.util.List;

import pro.kensait.jakartaee.employee.service.EmployeePage;

/** 社員一覧の応答の本文。要素は 1 件の応答と同じ EmployeeResponse で返す */
public record EmployeePageResponse(
        List<EmployeeResponse> content,
        int totalPages,
        long totalElements) {

    public static EmployeePageResponse from(EmployeePage page) {
        return new EmployeePageResponse(
                page.content().stream().map(EmployeeResponse::from).toList(),
                page.totalPages(),
                page.totalElements());
    }
}
