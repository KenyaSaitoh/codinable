package pro.kensait.spring.employee.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/*
 * 異動をメールで知らせるリスナー（教材用。実際にはメールを送らず、ログと sent に残すだけ）
 * application.yml の transfer.mail.fail を true にすると例外を送出し、
 * 失敗が AsyncConfig の AsyncUncaughtExceptionHandler に届くことを確かめられる（異動履歴の記録は止まらない）
 */
@Component
public class TransferMailNotifier {
    private static final Logger logger = LoggerFactory.getLogger(TransferMailNotifier.class);

    /** 送ったことにした通知と、それを処理したスレッド */
    public record Sent(EmployeeTransferred event, Thread thread) {
    }

    private final List<Sent> sent = new CopyOnWriteArrayList<>();
    private final boolean fail;

    public TransferMailNotifier(@Value("${transfer.mail.fail:false}") boolean fail) {
        this.fail = fail;
    }

    @Async("employeeExecutor") // 【1】
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 【2】
    public void notify(EmployeeTransferred event) {
        if (fail) {
            throw new IllegalStateException("メールサーバーに接続できません（教材用の失敗） employeeId="
                    + event.employeeId());
        }
        sent.add(new Sent(event, Thread.currentThread()));
        logger.info("[ TransferMailNotifier#notify ] 異動をメールで知らせました {} {} -> {} thread={}",
                event.employeeName(), event.fromDepartmentId(), event.toDepartmentId(), Thread.currentThread());
    }

    public List<Sent> sent() {
        return List.copyOf(sent);
    }
}
