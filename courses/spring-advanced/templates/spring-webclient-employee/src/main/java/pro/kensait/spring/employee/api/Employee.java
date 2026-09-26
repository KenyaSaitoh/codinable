package pro.kensait.spring.employee.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
/*
 * 社員を表すレコード（部署情報をフラットに保持する）
 */
public record Employee(
        Integer employeeId,
        @NotBlank @Size(max = 20) String employeeName,
        @NotNull Integer departmentId,
        String departmentName,
        @Size(max = 40) String jobName,
        @NotNull @PositiveOrZero Integer salary,
        LocalDate entranceDate) {
}
