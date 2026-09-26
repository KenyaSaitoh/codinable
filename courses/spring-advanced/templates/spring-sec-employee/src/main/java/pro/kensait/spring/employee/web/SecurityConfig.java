package pro.kensait.spring.employee.web;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
        http.formLogin(login -> login
                .successHandler((request, response, user) -> response.setStatus(204))
                .failureHandler((request, response, error) -> response.sendError(401)));
        http.logout(logout -> logout
                .logoutSuccessHandler((request, response, user) -> response.setStatus(204)));
        return http.build();
    }
    // パスワードencoderの実行
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    // usersの実行
    @Bean
    public UserDetailsService users(PasswordEncoder encoder) {
        // 講座専用のテストアカウント
        return new InMemoryUserDetailsManager(
                User.withUsername("Alice").password(encoder.encode("1111")).authorities("BASIC").build(),
                User.withUsername("Bob").password(encoder.encode("2222")).authorities("ADMIN").build());
    }
}
