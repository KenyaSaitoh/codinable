package pro.kensait.jakartaee.employee.service;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.DeliveryMode;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;

/*
 * 教材用（講座のコードには無い）: JSON として読めない本文を、社員イベントと同じキューへそのまま送る
 * 受信側の onMessage が実行時例外で終わり、再配送とデッドレターキューへの移動を確かめるために使う（curl/04_poison.sh）
 */
@ApplicationScoped
public class PoisonMessageSender {
    @Inject
    private JMSContext context;

    @Resource(lookup = "java:app/jms/EmployeeEventQueue")
    private Queue queue;

    public void send(String text) {
        context.createProducer()
                .setDeliveryMode(DeliveryMode.PERSISTENT)
                .setProperty("eventType", "UNKNOWN")
                .send(queue, text);
    }
}
