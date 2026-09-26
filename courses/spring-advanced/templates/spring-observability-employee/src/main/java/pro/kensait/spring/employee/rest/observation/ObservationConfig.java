package pro.kensait.spring.employee.rest.observation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;

/*
 * Micrometer Observation APIの設定を表すクラス
 */
@Configuration
public class ObservationConfig {
    // @Observedアノテーションを処理するアスペクトをBean登録する
    // （このBeanがないと@Observedを付与しても計装されない）
    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
}
