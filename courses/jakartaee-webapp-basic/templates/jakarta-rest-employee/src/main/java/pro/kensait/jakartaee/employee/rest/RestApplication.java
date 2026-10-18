package pro.kensait.jakartaee.employee.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/** REST API の URL は、コンテキストルートの後に /api が続く（/jakarta-rest-employee/api/employees） */
@ApplicationPath("/api")
public class RestApplication extends Application {
}
