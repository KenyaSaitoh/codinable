package pro.kensait.spring.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/*
 * Spring Cloud Config Serverを起動するクラス
 */
// Config Serverは、複数のアプリケーションの設定を一元管理し、
// HTTP経由で配信するためのサーバー（設定の一元管理・環境別の出し分けが目的）
// @EnableConfigServerを付けると、設定配信エンドポイント
@SpringBootApplication
@EnableConfigServer
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
