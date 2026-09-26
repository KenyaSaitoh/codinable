package pro.kensait.spring.employee.retry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
// Spring Retryを有効化する（@Retryableを機能させるために必須）
@EnableRetry
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
