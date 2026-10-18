package pro.kensait.jakartaee.employee.service;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import pro.kensait.jakartaee.employee.config.KafkaSettings;

/*
 * トピック employee-events の受信ループ
 * アプリケーションの起動時に、コンテナが管理するスレッド（既定の ManagedExecutorService）で poll を繰り返す
 * 記録をすべて処理できたらオフセットをコミットし、失敗したら最後にコミットした位置へ戻して読み直す
 */
@ApplicationScoped
public class EmployeeEventConsumer {
    private static final Logger logger = Logger.getLogger(EmployeeEventConsumer.class.getName());
    private static final Jsonb JSONB = JsonbBuilder.create();

    @Resource
    private ManagedExecutorService executor; // 【1】

    @Inject
    private KafkaSettings settings;
    @Inject
    private EmployeeEventHandler handler;

    private KafkaConsumer<String, String> consumer;
    private volatile boolean running = true;

    void start(@Observes Startup startup) { // 【2】
        consumer = new KafkaConsumer<>(settings.consumerProperties()); // 【3】
        executor.submit(this::pollLoop); // 【4】
    }

    @PreDestroy
    void stop() {
        running = false; // 【5】
        consumer.wakeup(); // 【6】
    }

    private void pollLoop() {
        try {
            consumer.subscribe(List.of("employee-events")); // 【1】
            while (running) {
                ConsumerRecords<String, String> records =
                        consumer.poll(Duration.ofSeconds(1)); // 【2】
                if (processAll(records)) { // 【3】
                    consumer.commitSync(); // 【4】
                } else {
                    rewindToCommitted(); // 【5】
                }
            }
        } catch (WakeupException e) {
            // 停止の合図なので、何もせずにループを抜ける
        } finally {
            consumer.close(); // 【6】
        }
    }

    // 記録を 1 件ずつ処理する。例外が出たら残りを処理せずに false を返す
    private boolean processAll(ConsumerRecords<String, String> records) {
        for (ConsumerRecord<String, String> record : records) {
            try {
                logger.info("[ EmployeeEventConsumer#processAll ] partition=" + record.partition()
                        + " offset=" + record.offset() + " key=" + record.key());
                handler.handle(JSONB.fromJson(record.value(), EmployeeEvent.class));
            } catch (RuntimeException e) {
                logger.log(Level.WARNING, "記録を処理できませんでした partition=" + record.partition()
                        + " offset=" + record.offset(), e);
                return false;
            }
        }
        return true;
    }

    private void rewindToCommitted() {
        Set<TopicPartition> assigned = consumer.assignment(); // 【1】
        Map<TopicPartition, OffsetAndMetadata> committed = consumer.committed(assigned); // 【2】
        for (TopicPartition partition : assigned) {
            OffsetAndMetadata offset = committed.get(partition);
            if (offset == null) {
                consumer.seekToBeginning(List.of(partition)); // 【3】
            } else {
                consumer.seek(partition, offset.offset()); // 【4】
            }
        }
    }
}
