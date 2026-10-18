package pro.kensait.jakartaee.employee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import pro.kensait.jakartaee.employee.service.EmployeeEvent;

/*
 * 受信したイベントの記録（RECEIVED_EVENT テーブル。主キーは eventId）
 */
@Entity
@Table(name = "RECEIVED_EVENT")
public class ReceivedEvent {
    @Id
    @Column(name = "EVENT_ID")
    private String eventId;
    @Column(name = "EVENT_TYPE")
    private String eventType;
    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;
    @Column(name = "EMPLOYEE_NAME")
    private String employeeName;
    @Column(name = "OCCURRED_AT")
    private LocalDateTime occurredAt;
    @Column(name = "RECEIVED_AT")
    private LocalDateTime receivedAt;

    public static ReceivedEvent of(EmployeeEvent event, LocalDateTime receivedAt) {
        ReceivedEvent r = new ReceivedEvent();
        r.eventId = event.eventId();
        r.eventType = event.eventType();
        r.employeeId = event.employeeId();
        r.employeeName = event.employeeName();
        r.occurredAt = event.occurredAt();
        r.receivedAt = receivedAt;
        return r;
    }

    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public Integer getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
}
