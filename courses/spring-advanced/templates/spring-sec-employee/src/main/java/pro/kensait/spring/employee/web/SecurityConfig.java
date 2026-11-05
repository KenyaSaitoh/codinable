package pro.kensait.spring.employee.web;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    // フィルタチェーンの設定
    @Bean
    public SecurityFilterChain security(HttpSecurity http) throws Exception {
        // セッション認証ではCSRF保護を維持し、SPAは /csrf でトークンを取得する
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/csrf", "/login", "/error").permitAll()
                .requestMatchers(HttpMethod.GET, "/employees", "/employees/**", "/session").authenticated()
                .requestMatchers("/employees", "/employees/**").hasAuthority("ADMIN")
                .anyRequest().authenticated());
        http.exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, error) -> response.sendError(401))
                .accessDeniedHandler((request, response, error) -> response.sendError(403)));
        http.formLogin(login -> login
                .successHandler((request, response, user) -> response.setStatus(204))
                .failureHandler((request, response, error) -> response.sendError(401)));
        // ログインに成功したらセッション ID を変える（セッション固定化攻撃の対策。既定の動作を明示している）
        http.sessionManagement(session -> session
                .sessionFixation(fixation -> fixation.changeSessionId()));
        http.logout(logout -> logout
                .logoutSuccessHandler((request, response, user) -> response.setStatus(204)));
        return http.build();
    }
    // パスワードのハッシュ化の部品
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    // 講座専用の利用者
    @Bean
    public UserDetailsService users(PasswordEncoder encoder) {
        // 講座専用のテストアカウント
        String aliceHash = encoder.encode("1111");
        // 保存される形式を確かめるために出す（実際のアプリではパスワードのハッシュをログに出さない）
        logger.info("Alice のパスワードの保存形式 => {}", aliceHash);
        return new InMemoryUserDetailsManager(
                User.withUsername("Alice").password(aliceHash).authorities("BASIC").build(),
                User.withUsername("Bob").password(encoder.encode("2222")).authorities("ADMIN").build());
    }
}
