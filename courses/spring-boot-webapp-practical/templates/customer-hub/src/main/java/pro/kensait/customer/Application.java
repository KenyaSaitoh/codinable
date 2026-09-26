package pro.kensait.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableAutoConfiguration
@EnableJpaRepositories({"pro.kensait.customer.repository"})
// Spring Boot 4.0では@EntityScanが不要（@SpringBootApplicationで自動スキャン）
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

