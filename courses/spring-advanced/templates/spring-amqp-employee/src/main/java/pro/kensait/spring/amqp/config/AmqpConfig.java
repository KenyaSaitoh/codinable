package pro.kensait.spring.amqp.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
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
    // キュー名・エクスチェンジ名・ルーティングキー
    // AMQPでは、プロデューサーは「エクスチェンジ」にメッセージを送信し、
    // エクスチェンジが「バインディング」の定義に従って「キュー」に振り分ける
    // （Kafkaの「トピックに直接送信する」方式との大きな違い）
    public static final String QUEUE_NAME = "employee-queue";
    public static final String EXCHANGE_NAME = "employee-exchange";
    public static final String ROUTING_KEY = "employee.event";

    // キュー定義：コンシューマがメッセージを取り出す待ち行列
    // （@Beanで宣言しておくと、RabbitMQへの接続時にブローカー上へ自動作成される）
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME);
    }

    // エクスチェンジ定義：ダイレクトエクスチェンジは、
    // ルーティングキーが完全一致するキューにメッセージを配送
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    // バインディング定義：エクスチェンジとキューをルーティングキーで結び付ける
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    // メッセージコンバーター定義：EmployeeEventをJSONに変換して送信する
    // （デフォルトはJavaの直列化形式のため、言語非依存のJSONに差し替える
    //   旧来のJackson2JsonMessageConverterはSpring AMQP 4で非推奨となったため、
    //   後継のJacksonJsonMessageConverterを使用する）
    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
