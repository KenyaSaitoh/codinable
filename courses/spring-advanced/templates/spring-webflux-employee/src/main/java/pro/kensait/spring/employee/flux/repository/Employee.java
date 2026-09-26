package pro.kensait.spring.employee.flux.repository;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/*
 * 社員を表すレコード（部署情報をフラットに保持する）
 */
public record Employee(
        Integer employeeId,     // 社員ID
        @NotBlank @Size(max = 20) String employeeName,    // 社員名
        @NotNull Integer departmentId,   // 部署ID
        String departmentName,  // 部署名
        String jobName,         // 職種
        @NotNull @PositiveOrZero Integer salary,         // 給与
        LocalDate entranceDate      // 入社日
        ) {
}
