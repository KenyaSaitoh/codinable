package pro.kensait.jakartaee.employee.service;

import java.time.LocalDateTime;
import java.util.UUID;
import pro.kensait.jakartaee.employee.entity.Employee;

/*
 * 社員イベント（本文の JSON に対応するレコード）
 * eventId は送信側が付ける UUID で、同じイベントを再送しても変わらない（受信側が重複を見分ける根拠になる）
 */
public record EmployeeEvent(
        String eventId,         // イベントID
        String eventType,       // EMPLOYEE_CREATED・EMPLOYEE_UPDATED・EMPLOYEE_DELETED
        Integer employeeId,     // 社員ID
        String employeeName,    // 社員名
        LocalDateTime occurredAt) { // 出来事が起きた日時

    public static EmployeeEvent created(Employee employee) {
        return new EmployeeEvent(UUID.randomUUID().toString(), "EMPLOYEE_CREATED",
                employee.getEmployeeId(), employee.getEmployeeName(), LocalDateTime.now());
    }

    // eventId や occurredAt が無ければ補う（POST /api/events で送られたイベント用）
    public EmployeeEvent withDefaults() {
        return new EmployeeEvent(eventId != null ? eventId : UUID.randomUUID().toString(),
                eventType != null ? eventType : "EMPLOYEE_UPDATED", employeeId, employeeName,
                occurredAt != null ? occurredAt : LocalDateTime.now());
    }
}
