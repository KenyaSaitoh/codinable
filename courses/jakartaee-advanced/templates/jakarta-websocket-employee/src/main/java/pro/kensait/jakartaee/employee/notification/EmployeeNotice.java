package pro.kensait.jakartaee.employee.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/*
 * 社員通知（WebSocket で送受信する JSON に対応するレコード）
 * 社員マスターに ID が存在するかの照会はしない
 */
public record EmployeeNotice(
        @NotNull @Positive Integer employeeId,          // 社員ID
        @NotBlank @Size(max = 20) String employeeName,  // 社員名
        @NotBlank @Size(max = 200) String message,      // 通知内容
        LocalDateTime occurredAt) {                     // 通知の時刻（サーバーが付ける）

    // 送信者が付けた時刻は信頼せず、サーバーの時刻に置き換えたコピーを作る
    public EmployeeNotice withOccurredAt(LocalDateTime time) {
        return new EmployeeNotice(employeeId, employeeName, message, time);
    }
}
