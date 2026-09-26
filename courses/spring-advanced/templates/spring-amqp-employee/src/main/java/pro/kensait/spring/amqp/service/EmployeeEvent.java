package pro.kensait.spring.amqp.service;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/*
 * 社員イベント（社員が登録されたことなどを通知するメッセージ）を表すレコード
 */
public record EmployeeEvent(
        // イベント種別（"EMPLOYEE_CREATED"など）
        @NotBlank @Pattern(regexp = "EMPLOYEE_(CREATED|UPDATED|DELETED)") String eventType,

        // 社員ID
        @NotNull @Positive Integer employeeId,

        // 社員名
        @NotBlank @Size(max = 20) String employeeName,

        // 発生日時
        @NotNull LocalDateTime occurredAt
        ) {
}
