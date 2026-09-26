package pro.kensait.spring.amqp.api;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pro.kensait.spring.amqp.service.EmployeeEvent;
import pro.kensait.spring.amqp.service.EventSendService;

/*
 * 社員イベント送信のREST APIを担うクラス
 */
@RestController
@RequestMapping("/events")
public class EventApi {
    private static final Logger logger = LoggerFactory.getLogger(EventApi.class);

    // インジェクションポイント
    @Autowired
    private EventSendService eventSendService;

    // unavailableの実行
    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> unavailable(IllegalStateException error) {
        return ResponseEntity.status(503).body("ブローカーへの送信を確認できません受信履歴を確認してください");
    }

    // APIメソッド：社員イベントのRabbitMQへの送信
    @PostMapping
    public ResponseEntity<EmployeeEvent> send(
            @Valid @RequestBody EmployeeEvent event) {
        logger.info("[ EventApi#send ]");

        // ビジネスロジックを呼び出し、社員イベントをRabbitMQに送信する
        eventSendService.sendEvent(event);

        // メッセージング経由の処理は非同期のため、「受理」を意味するステータス202で
        // 受け付けたイベントをボディに保持するResponseEntityを生成し、返す
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(event);
    }
}
