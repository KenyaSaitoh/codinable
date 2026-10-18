package pro.kensait.jakartaee.employee.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/*
 * 社員APIの接続先とタイムアウトを、MicroProfile Config から受け取る CDI Bean
 * 値は序列値（システムプロパティ 400 > 環境変数 300 > microprofile-config.properties 100）で決まった 1 つだけが注入される
 */
@ApplicationScoped
public class EmployeeApiSettings {
    @Inject
    @ConfigProperty(name = "employee.api.url") // 【1】既定値が無いので、どの設定ソースにも無ければ配置が失敗する
    String url;

    @Inject
    @ConfigProperty(name = "employee.api.read-timeout-ms", defaultValue = "3000") // 【2】
    long readTimeoutMs;

    @Inject
    @ConfigProperty(name = "employee.api.token") // 【3】必須ではない設定
    Optional<String> token;

    public String url() { return url; }
    public Duration readTimeout() { return Duration.ofMillis(readTimeoutMs); }
    public boolean hasToken() { return token.isPresent(); }
}
