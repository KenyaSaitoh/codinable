package pro.kensait.jakartaee.employee.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.jms.JMSDestinationDefinition;

/*
 * 受信側（consumer）のキューの定義。送信側と同じ destinationName（EmployeeEventQueue）を指す
 * JNDI 名はアプリケーションごとの名前で、実際にメッセージが置かれる場所は destinationName で決まる
 */
@JMSDestinationDefinition(
        name = "java:app/jms/EmployeeEventQueue",
        interfaceName = "jakarta.jms.Queue",
        destinationName = "EmployeeEventQueue")
@ApplicationScoped
public class MessagingConfig {
}
