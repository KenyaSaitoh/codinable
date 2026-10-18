package pro.kensait.jakartaee.employee.notification;

/*
 * 送信元だけへ返す入力エラー（{"error":"..."} の形で送る）
 */
public record NoticeError(String error) {
    public static final NoticeError INVALID = new NoticeError("社員ID・社員名・通知内容を確認してください");
}
