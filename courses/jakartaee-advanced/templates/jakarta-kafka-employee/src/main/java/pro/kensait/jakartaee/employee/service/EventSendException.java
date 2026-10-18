package pro.kensait.jakartaee.employee.service;

/*
 * Kafka への送信の失敗・待ち時間の超過（EventSendExceptionMapper が 503 にする）
 */
public class EventSendException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EventSendException(Throwable cause) {
        super("イベントを送信できませんでした: " + cause, cause);
    }
}
