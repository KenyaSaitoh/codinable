package pro.kensait.jakartaee.employee.api;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** 確認用 API の社員（部署の情報をフラットに持つ） */
public record Employee(
        Integer employeeId,
        @NotBlank @Size(max = 20) String employeeName,
        @NotNull Integer departmentId,
        String departmentName,
        @Size(max = 40) String jobName,
        @NotNull @PositiveOrZero Integer salary,
        LocalDate entranceDate) {
}
