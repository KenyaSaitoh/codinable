import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.TopicExistsException;

public class KafkaExample {
    public static void main(String[] args) throws Exception {
        String topic = "codinable-example";
        String message = "Hello Kafka! " + UUID.randomUUID();
        var config = new Properties();
        config.put("bootstrap.servers", System.getenv().getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "127.0.0.1:9092"));

        // A topic retains records, so the example can be run repeatedly.
        try (var admin = Admin.create(config)) {
            try {
                admin.createTopics(List.of(new NewTopic(topic, 1, (short) 1))).all().get(15, TimeUnit.SECONDS);
            } catch (ExecutionException e) {
                if (!(e.getCause() instanceof TopicExistsException)) throw e;
            }
        }

        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        try (var producer = new KafkaProducer<String, String>(config)) {
            var metadata = producer.send(new ProducerRecord<>(topic, "greeting", message)).get(15, TimeUnit.SECONDS);
            System.out.printf("Sent: %s (partition=%d, offset=%d)%n", message, metadata.partition(), metadata.offset());
        }

        // A fresh group reads from the beginning. Find this run's message.
        config.put("group.id", "codinable-" + UUID.randomUUID());
        config.put("auto.offset.reset", "earliest");
        config.put("enable.auto.commit", "false");
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        try (var consumer = new KafkaConsumer<String, String>(config)) {
            consumer.subscribe(List.of(topic));
            long deadline = System.currentTimeMillis() + 20000;
            while (System.currentTimeMillis() < deadline) {
                for (var record : consumer.poll(Duration.ofMillis(500))) {
                    if (message.equals(record.value())) {
                        System.out.println("Received: " + record.value());
                        consumer.commitSync();
                        return;
                    }
                }
            }
            throw new IllegalStateException("Timed out waiting for the message");
        }
    }
}
