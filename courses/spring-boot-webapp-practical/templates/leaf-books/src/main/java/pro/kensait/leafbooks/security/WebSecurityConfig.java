package pro.kensait.leafbooks.security;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/*
 * Webセキュリティに関する設定
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;

    // セキュリティフィルターchainの実行
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("[ WebSecurityConfig#securityFilterChain ]");

        // CORS設定を有効化
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // CSRF設定（SPA対応：CookieベースのCSRFトークン）
        // 認証エンドポイントは簡易テストのためCSRFを無効化
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);
        
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/auth/**", "/api/orders/**")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler));

        // セッションをステートレスに設定
        http.sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 認可設定
        http.authorizeHttpRequests(auth -> auth
                // 認証不要のエンドポイント
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/images/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                // その他のAPIエンドポイントは認証必要
                .requestMatchers("/api/**").authenticated()
                // その他のリクエストは拒否
                .anyRequest().denyAll());

        // フォームログインを無効化
        http.formLogin(form -> form.disable());
        
        // HTTP Basicを無効化
        http.httpBasic(basic -> basic.disable());

        // ログアウト設定
        http.logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(200);
                })
                .deleteCookies("token")
                .permitAll());

        // JWT認証フィルターを追加
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // cors設定変換元の実行
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        logger.info("[ WebSecurityConfig#corsConfigurationSource ]");
        
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        configuration.setAllowCredentials(allowCredentials);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }

    // パスワードencoderの実行
    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.info("[ WebSecurityConfig#passwordEncoder ]");
        return new BCryptPasswordEncoder();
    }
}

