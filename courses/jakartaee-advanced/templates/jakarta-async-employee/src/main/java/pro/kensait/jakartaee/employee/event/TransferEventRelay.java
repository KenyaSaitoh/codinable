package pro.kensait.jakartaee.employee.event;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.NotificationOptions;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * コミットに成功した異動の同期イベントを受け取り、非同期イベントとして発火し直す中継役
 */
@ApplicationScoped
public class TransferEventRelay {
    private static final Logger logger = Logger.getLogger(
            TransferEventRelay.class.getName());

    @Inject
    private Event<EmployeeTransferred> asyncEvent;

    @Resource(lookup = "java:app/concurrent/EmployeeExecutor")
    private ManagedExecutorService executor;

    public void relay(@Observes(during = TransactionPhase.AFTER_SUCCESS) // 【1】
            EmployeeTransferred event) {
        logger.info("[ TransferEventRelay#relay ] コミット後に非同期イベントを発火します employeeId=" + event.employeeId());
        asyncEvent.fireAsync(event, NotificationOptions.ofExecutor(executor)) // 【2】
                .exceptionally(error -> { // 【3】
                    logger.log(Level.WARNING, "異動通知の一部が失敗しました", error);
                    for (Throwable cause : error.getSuppressed()) { // 【4】
                        logger.log(Level.WARNING, "失敗した通知", cause);
                    }
                    return null;
                });
    }
}
