package pro.kensait.jakartaee.employee.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/*
 * Kafka のクライアントの設定（接続先は MicroProfile Config の kafka.bootstrap.servers）
 * Codinable 同梱の Kafka（127.0.0.1:9092）は、▶ アプリ実行のときに自動で起動する
 */
@ApplicationScoped
public class KafkaSettings {
    @Inject
    @ConfigProperty(name = "kafka.bootstrap.servers", defaultValue = "127.0.0.1:9092")
    String bootstrapServers;

    // 送信側の設定
    public Properties producerProperties() {
        Properties p = new Properties();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        p.put(ProducerConfig.ACKS_CONFIG, "all");                  // 同期しているすべてのレプリカが記録してから受け付けとする
        p.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);     // 内部の再試行による重複を防ぐ
        p.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 5000);    // 再試行を含めて送信の完了を待つ上限
        p.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 3000);     // delivery.timeout.ms 以下にする必要がある
        p.put(ProducerConfig.LINGER_MS_CONFIG, 0);
        p.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 3000);           // 接続先の情報が得られないときに send が待つ上限
        return p;
    }

    // 受信側の設定
    public Properties consumerProperties() {
        Properties p = new Properties();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        p.put(ConsumerConfig.GROUP_ID_CONFIG, "employee-group");
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        p.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);    // オフセットは処理の成功後に自分でコミットする
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // コミット済みのオフセットが無いときは先頭から読む
        return p;
    }
}
