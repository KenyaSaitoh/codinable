package pro.kensait.leafbooks.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

/*
 * Webセキュリティに関する設定
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(
            WebSecurityConfig.class);

    // セキュリティフィルターchainの実行
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("[ WebSecurityConfig#securityFilterChain ]");

        http.csrf(Customizer.withDefaults());

        http.formLogin(formLogin -> formLogin
                .loginPage("/").permitAll() // 認証未済だと自動的に遷移する
                .defaultSuccessUrl("/toSelect"));

        http.authorizeHttpRequests(auth -> auth
                // 静的リソースへのアクセスを許可
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                .requestMatchers(HttpMethod.POST, "/processLogin").permitAll() // 必須!
                .requestMatchers(HttpMethod.GET, "/toRegister").permitAll() // 必須!
                .requestMatchers(HttpMethod.POST, "/register").permitAll() // 必須!
                .requestMatchers(HttpMethod.GET,
                        "/processLogin",
                        "/register",
                        "/addBook",
                        "/removeBook",
                        "/clear",
                        "/fix",
                        "/order",
                        "/processLogout").denyAll() // GETの直接アクセスは禁止
                .anyRequest().authenticated());

        http.logout((logout) -> logout
            .logoutUrl("/processLogout") // Spring Security 7.0: logoutUrl を使用
            .logoutSuccessUrl("/logoutSuccess") // デフォルトは"login?logout"
            .invalidateHttpSession(true)
            .permitAll());

        // Spring Security 6.x: SecurityContextRepositoryを明示的に設定
        http.securityContext(securityContext -> 
            securityContext.securityContextRepository(securityContextRepository()));

        return http.build();
    }

    // パスワードencoderの実行
    @Bean
    public PasswordEncoder passwordEncoder(){
        logger.info("[ WebSecurityConfig#passwordEncoder ]");
        return new BCryptPasswordEncoder();
    }

    // リクエストキャッシュの実行
    @Bean
    public RequestCache requestCache(){
        logger.info("[ WebSecurityConfig#requestCache ]");
        return new HttpSessionRequestCache();
    }

    // セキュリティコンテキストリポジトリの実行
    @Bean
    public SecurityContextRepository securityContextRepository() {
        logger.info("[ WebSecurityConfig#securityContextRepository ]");
        return new HttpSessionSecurityContextRepository();
    }
}

