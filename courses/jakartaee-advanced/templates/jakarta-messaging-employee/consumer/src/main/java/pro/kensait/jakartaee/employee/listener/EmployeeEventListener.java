package pro.kensait.jakartaee.employee.listener;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.util.logging.Logger;
import pro.kensait.jakartaee.employee.service.EmployeeEvent;
import pro.kensait.jakartaee.employee.service.EmployeeEventHandler;

/*
 * キュー EmployeeEventQueue の社員イベントを受信するメッセージ駆動Bean
 * onMessage はコンテナが開始したトランザクションの中で呼ばれ、正常に終わってコミットされると確認応答になる
 * 実行時例外を送出するとロールバックされ、メッセージは再配送される
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup",
                propertyValue = "java:app/jms/EmployeeEventQueue"), // 【1】
        @ActivationConfigProperty(propertyName = "destinationType",
                propertyValue = "jakarta.jms.Queue") }) // 【2】
public class EmployeeEventListener implements MessageListener {
    private static final Logger logger = Logger.getLogger(EmployeeEventListener.class.getName());
    private static final Jsonb JSONB = JsonbBuilder.create();

    @Inject
    private EmployeeEventHandler handler;

    @Override
    public void onMessage(Message message) { // 【3】
        try {
            logger.info("[ EmployeeEventListener#onMessage ] JMSMessageID=" + message.getJMSMessageID()
                    + " JMSXDeliveryCount=" + message.getIntProperty("JMSXDeliveryCount"));
            EmployeeEvent event = JSONB.fromJson(
                    message.getBody(String.class), EmployeeEvent.class); // 【4】
            handler.handle(event, message.getIntProperty("JMSXDeliveryCount")); // 【5】
        } catch (JMSException e) {
            throw new IllegalStateException("メッセージを読み取れません", e); // 【6】
        }
    }
}
