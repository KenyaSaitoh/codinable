package pro.kensait.jakartaee.employee.service;

import java.time.LocalDateTime;
import java.util.UUID;

/*
 * 社員イベント（レッスン8.1.1と同じ JSON の形。eventId は送信側が付ける UUID）
 */
public record EmployeeEvent(
        String eventId,         // イベントID
        String eventType,       // EMPLOYEE_CREATED・EMPLOYEE_UPDATED・EMPLOYEE_DELETED
        Integer employeeId,     // 社員ID（Kafka の記録のキーにする）
        String employeeName,    // 社員名
        LocalDateTime occurredAt) { // 出来事が起きた日時

    // eventId や occurredAt が無ければ補う
    public EmployeeEvent withDefaults() {
        return new EmployeeEvent(eventId != null ? eventId : UUID.randomUUID().toString(),
                eventType != null ? eventType : "EMPLOYEE_UPDATED", employeeId, employeeName,
                occurredAt != null ? occurredAt : LocalDateTime.now());
    }
}
