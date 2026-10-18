package pro.kensait.jakartaee.employee.rest;

import java.time.LocalDate;

/*
 * 社員を表すレコード（チャプター3.1の社員APIと同じ項目）
 * 部署の無い社員（Victor）を表すため、departmentId と departmentName は参照型にしている
 */
public record EmployeeTO(
        Integer employeeId,      // 社員ID
        String employeeName,     // 社員名
        Integer departmentId,    // 部署ID
        String departmentName,   // 部署名
        String jobName,          // 職種
        Integer salary,          // 給与
        LocalDate entranceDate) { // 入社日
}
