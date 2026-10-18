package pro.kensait.jakartaee.employee.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/** 確認用 API の基底 URL は http://localhost:8080/employee-api/api */
@ApplicationPath("/api")
public class ApiApplication extends Application {
}
