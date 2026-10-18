package pro.kensait.jakartaee.employee.notification;

import jakarta.websocket.server.ServerEndpointConfig;
import org.eclipse.microprofile.config.ConfigProvider;

/*
 * ハンドシェイクの Origin ヘッダーを確かめるコンフィギュレーター
 * CDI Bean ではないので、MicroProfile Config は ConfigProvider から読む
 */
public class OriginCheckConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    public boolean checkOrigin(String originHeaderValue) { // 【1】
        String allowed = ConfigProvider.getConfig() // 【2】
                .getOptionalValue("notice.allowed-origin", String.class)
                .orElse("http://localhost:8080");
        return allowed.equals(originHeaderValue); // 【3】
    }
}
