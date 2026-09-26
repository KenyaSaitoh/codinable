package pro.kensait.spring.employee.notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
/*
 * 社員noticeを表すレコード
 */
public record EmployeeNotice(@NotNull @Positive Integer employeeId,
        @NotBlank @Size(max = 20) String employeeName,
        @NotBlank @Size(max = 200) String message, String occurredAt) {
}
