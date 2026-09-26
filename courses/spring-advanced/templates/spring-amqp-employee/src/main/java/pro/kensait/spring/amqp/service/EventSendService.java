package pro.kensait.spring.amqp.service;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import pro.kensait.spring.amqp.config.AmqpConfig;
/*
 * イベントsend機能のビジネスロジック
 */
@Service
public class EventSendService {
    private final RabbitTemplate template;
    // イベントsendの初期化
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "Spring owns the messaging template and its connection lifecycle")
    public EventSendService(RabbitTemplate template) {
        this.template = template;
    }
    // イベントの送信
    public void sendEvent(EmployeeEvent event) {
        try {
            CorrelationData correlation = new CorrelationData(java.util.UUID.randomUUID().toString());
            template.convertAndSend(AmqpConfig.EXCHANGE_NAME, AmqpConfig.ROUTING_KEY, event, correlation);
            var confirm = correlation.getFuture().get(5, TimeUnit.SECONDS);
            if (!confirm.ack() || correlation.getReturned() != null) {
                throw new IllegalStateException("Broker rejected or returned the event");
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("送信待ちが中断されました", error);
        } catch (Exception error) {
            throw new IllegalStateException("送信確認に失敗しました", error);
        }
    }
}
