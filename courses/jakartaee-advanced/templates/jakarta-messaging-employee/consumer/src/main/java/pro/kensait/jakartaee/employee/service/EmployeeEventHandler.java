package pro.kensait.jakartaee.employee.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import pro.kensait.jakartaee.employee.entity.ReceivedEvent;

/*
 * 受信したイベントを RECEIVED_EVENT に記録する（eventId が主キー。同じイベントは 2 回記録しない）
 * MDB の onMessage のトランザクションに参加し、記録と確認応答が同じトランザクションで確定する
 */
@ApplicationScoped
public class EmployeeEventHandler {
    private static final Logger logger = Logger.getLogger(EmployeeEventHandler.class.getName());

    @PersistenceContext(unitName = "consumerPU")
    private EntityManager em;

    @Transactional(Transactional.TxType.MANDATORY) // 【1】
    public void handle(EmployeeEvent event, int deliveryCount) {
        if (deliveryCount > 1) { // 【2】
            logger.log(Level.WARNING, "再配送されたイベント: {0}（{1}回目）",
                    new Object[] { event.eventId(), deliveryCount });
        }
        if (em.find(ReceivedEvent.class, event.eventId()) != null) { // 【3】
            logger.info("[ EmployeeEventHandler#handle ] 処理済みのイベントです eventId=" + event.eventId());
            return;
        }
        em.persist(ReceivedEvent.of(event, LocalDateTime.now())); // 【4】
        logger.info("[ EmployeeEventHandler#handle ] 記録しました eventId=" + event.eventId()
                + " " + event.eventType() + " " + event.employeeName());
    }
}
