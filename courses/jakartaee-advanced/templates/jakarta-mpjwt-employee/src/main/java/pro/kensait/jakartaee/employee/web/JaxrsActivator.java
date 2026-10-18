package pro.kensait.jakartaee.employee.web;

import jakarta.annotation.security.DeclareRoles;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.auth.LoginConfig;

/*
 * Jakarta REST の基点（/jakarta-mpjwt-employee/api）と、MicroProfile JWT による認証の有効化
 * Authorization: Bearer のアクセストークンを検証し、groups クレームをロールに対応付ける
 */
@LoginConfig(authMethod = "MP-JWT", realmName = "employee-api") // 【1】
@DeclareRoles({"BASIC", "ADMIN"}) // 【2】
@ApplicationPath("/api")
public class JaxrsActivator extends Application {
}
