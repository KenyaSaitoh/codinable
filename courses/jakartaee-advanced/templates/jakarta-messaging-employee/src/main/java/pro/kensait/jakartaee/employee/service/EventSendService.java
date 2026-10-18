package pro.kensait.jakartaee.employee.service;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.DeliveryMode;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

/*
 * 社員イベントを JMSContext でキューへ送る
 * トランザクションの中で呼ばれると送信もそのトランザクションに参加し、コミットした時点で確定する
 */
@ApplicationScoped
public class EventSendService {
    private static final Jsonb JSONB = JsonbBuilder.create();

    @Inject
    private JMSContext context; // 【1】

    @Resource(lookup = "java:app/jms/EmployeeEventQueue")
    private Queue queue; // 【2】

    public void send(EmployeeEvent event) {
        context.createProducer() // 【3】
                .setDeliveryMode(DeliveryMode.PERSISTENT) // 【4】
                .setProperty("eventType", event.eventType()) // 【5】
                .send(queue, JSONB.toJson(event)); // 【6】
    }
}
