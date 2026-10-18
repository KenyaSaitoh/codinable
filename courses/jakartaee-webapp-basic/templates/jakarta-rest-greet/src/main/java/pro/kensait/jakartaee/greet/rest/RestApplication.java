package pro.kensait.jakartaee.greet.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/** REST API の URL は、コンテキストルートの後に /api が続く（/jakarta-rest-greet/api/greet/...） */
@ApplicationPath("/api")
public class RestApplication extends Application {
}
