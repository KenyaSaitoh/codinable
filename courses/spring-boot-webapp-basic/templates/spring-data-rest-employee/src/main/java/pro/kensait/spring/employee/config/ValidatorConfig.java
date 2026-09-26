package pro.kensait.spring.employee.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
/** Controllerの@Validに代わり、RepositoryイベントにBean Validationを登録する */
@Configuration
public class ValidatorConfig implements RepositoryRestConfigurer {
    // バリデーターの実行
    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    // configurevalidatingリポジトリイベントlistenerの実行
    @Override
    public void configureValidatingRepositoryEventListener(
            ValidatingRepositoryEventListener listener) {
        listener.addValidator("beforeCreate", validator());
        listener.addValidator("beforeSave", validator());
    }
}
