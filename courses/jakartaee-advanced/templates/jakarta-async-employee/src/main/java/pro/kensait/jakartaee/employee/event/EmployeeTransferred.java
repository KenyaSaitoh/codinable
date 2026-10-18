package pro.kensait.jakartaee.employee.event;

import java.time.LocalDateTime;

/*
 * 異動のイベント。受信側がデータベースを読み直さなくても通知や履歴を組み立てられる項目を持つ
 * 別々のスレッドで動く複数のオブザーバーが書き換えられないよう、レコードにしている
 */
public record EmployeeTransferred(
        Integer employeeId,        // 社員ID
        String employeeName,       // 社員名
        Integer fromDepartmentId,  // 異動前の部署ID
        Integer toDepartmentId,    // 異動後の部署ID
        LocalDateTime occurredAt) { // 異動を登録した日時
}
