package pro.kensait.leafbooks.config;

import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

/*
 * appに関する設定
 */
@Configuration
public class AppConfig {
    // RESTテンプレートの実行
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // UTF-8エンコーディングを設定
        restTemplate.getMessageConverters().add(0,
                new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

    // RESTクライアントの実行
    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }
}
