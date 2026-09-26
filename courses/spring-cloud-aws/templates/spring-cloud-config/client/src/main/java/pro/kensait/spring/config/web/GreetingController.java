package pro.kensait.spring.config.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * Config Serverから取得した設定値を返すコントローラーを担うクラス
 */
// @RefreshScopeは、Spring Cloud Contextが提供するスコープで、
// このBeanをプロキシ経由で参照させるPOST /actuator/refresh が呼ばれると、
// Config Serverから設定を再取得してEnvironmentを更新し、変更があったプロパティに対して
// EnvironmentChangeEventが発行されるその際、@RefreshScopeのBeanは破棄され、
// 次のアクセス時にプロキシが新しいEnvironmentの値でBeanを再生成する
// （そのため@Valueで注入した値が、アプリケーションを再起動せずに反映される）
@RestController
@RefreshScope
public class GreetingController {
    private static final Logger logger = LoggerFactory.getLogger(
            GreetingController.class);

    // Config Serverから取得した挨拶メッセージ（greeting.message）
    @Value("${greeting.message}")
    private String greetingMessage;

    // APIメソッド：挨拶メッセージの返却
    @GetMapping("/greeting")
    public String greeting() {
        logger.info("[ GreetingController#greeting ]");
        // Config Serverから取得した設定値をそのまま応答する
        return greetingMessage;
    }
}
