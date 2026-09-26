package pro.kensait.spring.employee.rest.health;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

/*
 * /actuator/infoにカスタム情報を追加するコントリビューターを表すクラス
 * （application.ymlのinfo.app.*による静的な情報とは異なり、
 * 　Javaコードで動的に生成した情報を追加できる）
 */
@Component
public class AppInfoContributor implements InfoContributor {
    // アプリケーションの起動日時（このBeanが生成された日時）
    private final LocalDateTime startupTime = LocalDateTime.now();

    // 情報追加メソッド：/actuator/infoのレスポンスに"runtime"セクションの追加
    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("runtime", Map.of(
                "startupTime", startupTime.toString(),
                "javaVersion", System.getProperty("java.version"),
                "availableProcessors", Runtime.getRuntime().availableProcessors()));
    }
}
