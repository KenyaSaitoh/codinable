package pro.kensait.jakartaee.employee.client;

import java.time.LocalDate;

/** 社員を表す転送オブジェクト（確認用 API が返す社員の JSON に対応） */
public record EmployeeTO(
        Integer employeeId,     // 社員ID
        String employeeName,    // 社員名
        Integer departmentId,   // 部署ID
        String departmentName,  // 部署名
        String jobName,         // 職種
        Integer salary,         // 給与
        LocalDate entranceDate  // 入社日
        ) {
}
