package pro.kensait.jakartaee.employee.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigValue;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import pro.kensait.jakartaee.employee.config.EmployeeApiSettings;

/*
 * 設定値が最終的にどう決まったかを JSON で返す（GET /api/settings）
 * 環境変数（EMPLOYEE_API_READ_TIMEOUT_MS など）やシステムプロパティで上書きすると、ここに出る値が変わる
 */
@Path("/settings")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class SettingsResource {
    @Inject
    EmployeeApiSettings settings;

    // 他の設定値を ${...} で参照する式は、展開した結果が注入される
    @Inject
    @ConfigProperty(name = "employee.api.employees-url")
    String employeesUrl;

    @Inject
    @ConfigProperty(name = "employee.api.connect-timeout-ms")
    long connectTimeoutMs;

    // どの設定ソースの値が使われたかを調べるために、Config そのものも受け取る
    @Inject
    Config config;

    public record SettingsView(String url, String employeesUrl, long connectTimeoutMs,
            long readTimeoutMs, boolean hasToken, String readTimeoutSource, int readTimeoutOrdinal) {
    }

    @GET
    public SettingsView get() {
        // employee.api.read-timeout-ms の値を、どの設定ソース（序列値）から読んだか
        ConfigValue readTimeout = config.getConfigValue("employee.api.read-timeout-ms");
        return new SettingsView(settings.url(), employeesUrl, connectTimeoutMs,
                settings.readTimeout().toMillis(), settings.hasToken(),
                readTimeout.getSourceName(), readTimeout.getSourceOrdinal());
    }
}
