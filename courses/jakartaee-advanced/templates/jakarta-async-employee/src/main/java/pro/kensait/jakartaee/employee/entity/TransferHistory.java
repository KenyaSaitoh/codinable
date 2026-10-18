package pro.kensait.jakartaee.employee.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/*
 * 異動履歴のエンティティ（TRANSFER_HISTORY テーブル）
 */
@Entity
@Table(name = "TRANSFER_HISTORY")
public class TransferHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "HISTORY_ID")
    private Integer historyId;
    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;
    @Column(name = "FROM_DEPARTMENT_ID")
    private Integer fromDepartmentId;
    @Column(name = "TO_DEPARTMENT_ID")
    private Integer toDepartmentId;
    @Column(name = "OCCURRED_AT")
    private LocalDateTime occurredAt;

    public TransferHistory() {
    }

    public TransferHistory(Integer employeeId, Integer fromDepartmentId, Integer toDepartmentId,
            LocalDateTime occurredAt) {
        this.employeeId = employeeId;
        this.fromDepartmentId = fromDepartmentId;
        this.toDepartmentId = toDepartmentId;
        this.occurredAt = occurredAt;
    }

    public Integer getHistoryId() { return historyId; }
    public Integer getEmployeeId() { return employeeId; }
    public Integer getFromDepartmentId() { return fromDepartmentId; }
    public Integer getToDepartmentId() { return toDepartmentId; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}
