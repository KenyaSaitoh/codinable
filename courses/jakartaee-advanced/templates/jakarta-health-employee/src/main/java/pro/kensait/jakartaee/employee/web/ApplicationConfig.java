package pro.kensait.jakartaee.employee.web;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/*
 * Jakarta REST の基点（コンテキストルートの下の /api）
 */
@ApplicationPath("/api")
public class ApplicationConfig extends Application {
}
