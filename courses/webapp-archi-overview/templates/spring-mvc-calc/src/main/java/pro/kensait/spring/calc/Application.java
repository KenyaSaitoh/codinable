package pro.kensait.spring.calc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 * アプリケーションの起動クラス
 * 組み込みの Tomcat が立ち上がり、8080 番ポートでリクエストを待ち受ける
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
