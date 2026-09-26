package pro.kensait.spring.amqp.service;

import java.time.LocalDateTime;

/*
 * 社員イベント（社員が登録されたことなどを通知するメッセージ）を表すレコード
 */
public record EmployeeEvent(
        // イベント種別（"EMPLOYEE_CREATED"など）
        String eventType,

        // 社員ID
        Integer employeeId,

        // 社員名
        String employeeName,

        // 発生日時
        LocalDateTime occurredAt
        ) {
}
