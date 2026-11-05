package pro.kensait.spring.employee.service;

import java.time.LocalDate;

/*
 * 社員を表すレコード（部署の情報を平らに持つ）
 */
public record Employee(
        Integer employeeId,     // 社員ID
        String employeeName,    // 社員名
        Integer departmentId,   // 部署ID
        String departmentName,  // 部署名
        String jobName,         // 職種
        Integer salary,         // 給与
        LocalDate entranceDate) { // 入社日
}
