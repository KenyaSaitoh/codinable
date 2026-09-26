package pro.kensait.spring.employee.retry.service;

import java.time.LocalDate;

/*
 * 社員を表す転送オブジェクト（REST APIからの受信用）
 */
public record EmployeeTO(
        Integer employeeId,     // 社員ID
        String employeeName,    // 社員名
        Integer departmentId,   // 部署ID
        String departmentName,  // 部署名
        String jobName,         // 職種
        Integer salary,         // 給与
        LocalDate entranceDate      // 入社日
        ) {
}
