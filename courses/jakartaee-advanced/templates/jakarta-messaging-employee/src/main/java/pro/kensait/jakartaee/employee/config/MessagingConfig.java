package pro.kensait.jakartaee.employee.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.jms.JMSDestinationDefinition;

/*
 * 送信側（producer）のキューの定義
 * 受信側（consumer）も同じ destinationName の MessagingConfig を持ち、同じキューを介して連携する
 */
@JMSDestinationDefinition(
        name = "java:app/jms/EmployeeEventQueue", // 【1】
        interfaceName = "jakarta.jms.Queue", // 【2】
        destinationName = "EmployeeEventQueue") // 【3】
@ApplicationScoped
public class MessagingConfig {
}
