package pro.kensait.jakartaee.employee.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import pro.kensait.jakartaee.employee.config.KafkaSettings;

/*
 * 社員イベントを Kafka のトピック employee-events へ送る（キーは社員ID）
 * KafkaProducer はスレッドセーフなので、アプリケーション全体で 1 つを共有する
 * コンテナの資源ではないので、生成と後片付けはアプリケーションが行う
 */
@ApplicationScoped
public class EventSendService {
    private static final Jsonb JSONB = JsonbBuilder.create();

    @Inject
    private KafkaSettings settings;

    private KafkaProducer<String, String> producer;

    @PostConstruct
    void init() {
        producer = new KafkaProducer<>(settings.producerProperties()); // 【1】
    }

    public RecordMetadata send(EmployeeEvent event) {
        ProducerRecord<String, String> record = new ProducerRecord<>(
                "employee-events", // 【2】
                String.valueOf(event.employeeId()), // 【3】
                JSONB.toJson(event));
        try {
            return producer.send(record).get(6, TimeUnit.SECONDS); // 【4】
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EventSendException(e);
        } catch (ExecutionException | TimeoutException e) {
            throw new EventSendException(e); // 【5】
        }
    }

    @PreDestroy
    void close() {
        producer.close(); // 【6】
    }
}
