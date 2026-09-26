package pro.kensait.spring.amqp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/*
 * 社員イベントをRabbitMQから受信するビジネスロジックを表すクラス
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

    // リスナーメソッド：キュー"employee-queue"から社員イベントを受信する
    // AMQPでは、コンシューマは「キュー」を購読する
    // ・同じキューを複数のコンシューマが購読 => メッセージは分配される（1件は1つが処理）
    // ・複数のキューを同じエクスチェンジにバインド => 各キューが同じメッセージを受信できる
    // （Kafkaの「トピック＋コンシューマグループ」に対し、AMQPは
    //   「エクスチェンジ＋キュー＋バインディング」の組み合わせで配送を制御する）
    @RabbitListener(queues = "employee-queue")
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
