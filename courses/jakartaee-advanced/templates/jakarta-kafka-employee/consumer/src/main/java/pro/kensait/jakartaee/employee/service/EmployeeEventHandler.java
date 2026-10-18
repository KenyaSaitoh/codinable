package pro.kensait.jakartaee.employee.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.logging.Logger;
import pro.kensait.jakartaee.employee.entity.ReceivedEvent;

/*
 * 受信したイベントを RECEIVED_EVENT に記録する（eventId が主キー。同じイベントは 2 回記録しない）
 * 受信ループには呼び出し元のトランザクションが無いので、呼ぶたびにトランザクションを開始してコミットする
 * Kafka のオフセットのコミットはこのトランザクションに参加しない（DB を先に確定し、その後でオフセットをコミットする）
 */
@ApplicationScoped
public class EmployeeEventHandler {
    private static final Logger logger = Logger.getLogger(EmployeeEventHandler.class.getName());

    @PersistenceContext(unitName = "consumerPU")
    private EntityManager em;

    @Transactional // 【1】
    public void handle(EmployeeEvent event) {
        if (em.find(ReceivedEvent.class, event.eventId()) != null) { // 【2】
            logger.info("[ EmployeeEventHandler#handle ] 処理済みのイベントです eventId=" + event.eventId());
            return;
        }
        em.persist(ReceivedEvent.of(event, LocalDateTime.now())); // 【3】
        logger.info("[ EmployeeEventHandler#handle ] 記録しました eventId=" + event.eventId()
                + " " + event.eventType() + " " + event.employeeName());
    }
}
