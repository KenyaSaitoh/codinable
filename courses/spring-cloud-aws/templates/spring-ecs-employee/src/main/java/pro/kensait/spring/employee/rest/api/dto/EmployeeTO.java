package pro.kensait.spring.employee.rest.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/*
 * 社員を表す転送オブジェクト（部署情報をフラットに保持する）
 */
public record EmployeeTO(
        Integer employeeId,     // 社員ID
        @NotEmpty
        @Size(min = 1, max = 20)
        String employeeName,    // 社員名
        @NotNull
        Integer departmentId,   // 部署ID
        String departmentName,  // 部署名
        String jobName,         // 職種
        Integer salary,         // 給与
        LocalDate hireDate      // 入社日
        ) {
}
