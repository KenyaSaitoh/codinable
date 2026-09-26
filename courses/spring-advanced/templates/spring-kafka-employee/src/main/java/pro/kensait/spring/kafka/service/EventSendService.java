package pro.kensait.spring.kafka.service;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;
/*
 * イベントsend機能のビジネスロジック
 */
@Service
public class EventSendService {
    private final KafkaTemplate<String, EmployeeEvent> template;
    // イベントsendの初期化
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "Spring owns the messaging template and its connection lifecycle")
    public EventSendService(KafkaTemplate<String, EmployeeEvent> template) {
        this.template = template;
    }
    // イベントの送信
    public void sendEvent(EmployeeEvent event) {
        try {
            template.send("employee-events", event.employeeId().toString(), event).get(6, TimeUnit.SECONDS);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("送信待ちが中断されました", error);
        } catch (Exception error) {
            throw new IllegalStateException("送信確認に失敗しました", error);
        }
    }
}
