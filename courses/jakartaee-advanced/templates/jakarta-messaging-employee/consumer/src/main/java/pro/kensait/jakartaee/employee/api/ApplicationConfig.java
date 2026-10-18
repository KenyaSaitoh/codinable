package pro.kensait.jakartaee.employee.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/*
 * 受信側（consumer）の Jakarta REST の基点（/jakarta-messaging-employee-consumer/api）
 */
@ApplicationPath("/api")
public class ApplicationConfig extends Application {
}
