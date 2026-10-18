package pro.kensait.jakartaee.employee.web;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Map;

/*
 * Jakarta REST の基点（/jakarta-security-employee/api）
 * Payara の場合、リソースクラスの @RolesAllowed は Jersey の RolesAllowedDynamicFeature を
 * 登録したときだけ評価される（Jakarta REST の仕様では必須ではない）
 */
@ApplicationPath("/api")
public class ApplicationConfig extends Application {
    @Override
    public Map<String, Object> getProperties() {
        return Map.of("jersey.config.server.provider.classnames",
                "org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature");
    }
}
