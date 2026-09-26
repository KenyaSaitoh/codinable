package pro.kensait.spring.kafka.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/*
 * 社員イベントをKafkaから受信するビジネスロジックを表すクラス
 */
@Service
public class EventReceiveService {
    private static final Logger logger = LoggerFactory.getLogger(
            EventReceiveService.class);

    // UIで実際の受信を確認するための直近100件永続的な監査ログではない
    private final java.util.Deque<EmployeeEvent> received = new java.util.ArrayDeque<>();
    // 履歴の実行
    public synchronized java.util.List<EmployeeEvent> history() {
        return java.util.List.copyOf(received);
    }

    // リスナーメソッド：トピック"employee-events"から社員イベントを受信する
    // Kafkaでは、コンシューマは「コンシューマグループ」単位でトピックを購読する
    // ・同一グループ内 => パーティションが分担され、1件のイベントは1つのコンシューマだけが処理する
    // ・別グループ     => それぞれのグループが同じイベントを重複なく受信できる
    // （AMQPが「エクスチェンジ＋キュー＋バインディング」で配送を制御するのに対し、
    //   Kafkaは「トピック＋コンシューマグループ」で読み出しを制御する）
    @KafkaListener(topics = "employee-events", groupId = "employee-group")
    public synchronized void receiveEvent(EmployeeEvent event) {
        if (received.size() == 100) {
            received.removeFirst();
        }
        received.addLast(event);
        logger.info("[ EventReceiveService#receiveEvent ]");

        // 受信した社員イベントの内容をログに出力する
        logger.info("社員イベントを受信 => eventType: {}, employeeId: {}, "
                + "employeeName: {}, occurredAt: {}",
                event.eventType(), event.employeeId(),
                event.employeeName(), event.occurredAt());
    }
}
