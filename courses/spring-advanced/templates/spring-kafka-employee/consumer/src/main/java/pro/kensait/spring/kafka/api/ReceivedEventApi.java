package pro.kensait.spring.kafka.api;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.kensait.spring.kafka.service.EmployeeEvent;
import pro.kensait.spring.kafka.service.EventReceiveService;
/*
 * receivedイベントAPIの機能を提供するクラス
 */
@RestController
public class ReceivedEventApi {
    private final EventReceiveService service;
    // receivedイベントAPIの初期化
    public ReceivedEventApi(EventReceiveService service) {
        this.service = service;
    }
    // eventsの実行
    @GetMapping("/events")
    public List<EmployeeEvent> events() {
        return service.history();
    }
}
