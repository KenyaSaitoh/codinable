import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class RabbitExample {
    public static void main(String[] args) throws Exception {
        var factory = new ConnectionFactory();
        factory.setUri(System.getenv().getOrDefault("RABBITMQ_URL", "amqp://guest:guest@127.0.0.1:5672/"));
        factory.setConnectionTimeout(10000);
        String queue = "codinable-example";
        String message = "Hello RabbitMQ! " + UUID.randomUUID();

        try (var connection = factory.newConnection(); var channel = connection.createChannel()) {
            // Durable queue + persistent message. Acknowledge only after processing.
            channel.queueDeclare(queue, true, false, false, null);
            channel.confirmSelect();
            channel.basicPublish("", queue, MessageProperties.PERSISTENT_TEXT_PLAIN,
                    message.getBytes(StandardCharsets.UTF_8));
            channel.waitForConfirmsOrDie(10000);
            System.out.println("Sent: " + message);

            var received = new CountDownLatch(1);
            channel.basicQos(1);
            String consumerTag = channel.basicConsume(queue, false, (tag, delivery) -> {
                String body = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("Received: " + body);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                if (message.equals(body)) received.countDown();
            }, tag -> {});
            if (!received.await(20, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for the message");
            }
            channel.basicCancel(consumerTag);
        }
    }
}
