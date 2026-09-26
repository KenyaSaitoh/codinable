package pro.kensait.spring.employee.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy.RepositoryDetectionStrategies;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import pro.kensait.spring.employee.entity.Department;
import pro.kensait.spring.employee.entity.Employee;
import pro.kensait.spring.employee.entity.Job;
/** 自動公開する範囲と、確認用clientからのアクセスを設定する */
@Configuration
public class RestConfig implements RepositoryRestConfigurer {
    // configureリポジトリREST設定の実行
    @Override
    public void configureRepositoryRestConfiguration(
            RepositoryRestConfiguration config, CorsRegistry cors) {
        config.setRepositoryDetectionStrategy(RepositoryDetectionStrategies.ANNOTATED);
        config.exposeIdsFor(Employee.class, Department.class, Job.class);
        config.setReturnBodyOnCreate(true);
        config.setReturnBodyOnUpdate(true);
        config.setReturnBodyOnDelete(false);
        config.getExposureConfiguration().disablePutForCreation();
        readOnly(config, Department.class);
        readOnly(config, Job.class);
        cors.addMapping("/**")
                .allowedOrigins("http://localhost:5500", "http://127.0.0.1:5500")
                .allowedMethods("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("ETag", "Location");
    }

    // のみの取得
    private void readOnly(RepositoryRestConfiguration config, Class<?> entityType) {
        config.getExposureConfiguration().forDomainType(entityType)
                .withCollectionExposure((metadata, methods) -> methods.disable(HttpMethod.POST))
                .withItemExposure((metadata, methods) -> methods.disable(
                        HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.DELETE));
    }
}
