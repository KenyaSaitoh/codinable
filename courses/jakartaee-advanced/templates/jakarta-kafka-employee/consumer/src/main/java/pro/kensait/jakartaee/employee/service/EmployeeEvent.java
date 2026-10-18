package pro.kensait.jakartaee.employee.service;

import java.time.LocalDateTime;

/*
 * 受信する社員イベント（送信側と同じ JSON の形）
 */
public record EmployeeEvent(
        String eventId,         // イベントID（送信側が付ける UUID）
        String eventType,       // 種別
        Integer employeeId,     // 社員ID
        String employeeName,    // 社員名
        LocalDateTime occurredAt) { // 出来事が起きた日時
}
