package pro.kensait.jakartaee.employee.client;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/** 呼び出す側のアプリケーションの入口（POST /jakarta-mp-rest-client-employee/api/run） */
@ApplicationPath("/api")
public class RestApplication extends Application {
}
