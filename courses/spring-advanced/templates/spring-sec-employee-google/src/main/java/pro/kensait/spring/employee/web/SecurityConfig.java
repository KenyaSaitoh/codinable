package pro.kensait.spring.employee.web;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/*
 * Spring Securityの設定を表すクラス
 */
@Configuration
public class SecurityConfig {
    // セキュリティの実行
    @Bean
    public SecurityFilterChain security(HttpSecurity http,
            @Value("${frontend.url:http://localhost:5173}") String frontendUrl) throws Exception {
        // セッション認証ではCSRF保護を維持し、SPAは /csrf でトークンを取得する
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/csrf", "/login", "/oauth2/**", "/login/oauth2/**", "/error").permitAll()
                .requestMatchers(HttpMethod.GET, "/employees", "/employees/**", "/session").authenticated()
                .requestMatchers("/employees", "/employees/**").hasAuthority("ADMIN")
                .anyRequest().authenticated());
        http.exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, error) -> response.sendError(401))
                .accessDeniedHandler((request, response, error) -> response.sendError(403)));
        http.oauth2Login(login -> login
                .userInfoEndpoint(info -> info.oidcUserService(new EmployeeOidcUserService()))
                .defaultSuccessUrl(frontendUrl + "/", true)
                .failureUrl(frontendUrl + "/?loginError=true"));
        http.logout(logout -> logout
                .logoutSuccessHandler((request, response, user) -> response.setStatus(204)));
        return http.build();
    }

}
