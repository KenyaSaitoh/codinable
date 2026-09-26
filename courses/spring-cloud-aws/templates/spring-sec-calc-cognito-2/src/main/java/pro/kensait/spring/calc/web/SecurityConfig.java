package pro.kensait.spring.calc.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/*
 * Spring Securityの設定を表すクラス
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(
            SecurityConfig.class);

    // セキュリティフィルターchainの実行
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("[ SecurityConfig#securityFilterChain ]");

        http.csrf(Customizer.withDefaults());

        // OIDC（Cognito）によるログインを有効化する
        http.oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/toInput", true)
                .failureUrl("/loginError")
                .userInfoEndpoint(userInfo -> {
                    userInfo.oidcUserService(new CustomOidcUserService());
                }));

        http.logout((logout) -> logout
                .logoutUrl("/processLogout") // アクションの実装は不要、デフォルトは"logout"
                .logoutSuccessUrl("/logoutSuccess") // アクションの実装が必要、デフォルトは"login?logout"
                .invalidateHttpSession(true)
                .permitAll());

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                .requestMatchers(
                        "/loginError")
                .permitAll()
                .requestMatchers(HttpMethod.GET,
                        "/processLogout",
                        "/add",
                        "/subtract",
                        "/multiply",
                        "/divide")
                .denyAll()
                .requestMatchers(
                        "/viewHistory")
                .hasAuthority("ADMIN")
                .anyRequest().authenticated());

        return http.build();
    }
}
