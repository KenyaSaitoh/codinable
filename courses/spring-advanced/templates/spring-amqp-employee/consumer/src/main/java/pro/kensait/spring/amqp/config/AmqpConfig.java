package pro.kensait.spring.amqp.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * AMQP（RabbitMQ）の設定を表すクラス
 */
@Configuration
public class AmqpConfig {
    // 受信対象のキュー名
    public static final String QUEUE_NAME = "employee-queue";

    // キュー定義：プロデューサー側と同じ宣言（キューの宣言は冪等なので、
    // 送信側・受信側の両方で宣言してよい）
    // コンシューマを先に起動してもキューが存在するようにしておく
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME);
    }

    // メッセージコンバーター定義：受信したJSONをEmployeeEventに復元する
    // （旧来のJackson2JsonMessageConverterはSpring AMQP 4で非推奨となったため、
    //   後継のJacksonJsonMessageConverterを使用する）
    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
