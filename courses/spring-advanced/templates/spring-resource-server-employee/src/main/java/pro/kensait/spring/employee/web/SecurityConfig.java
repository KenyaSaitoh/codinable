package pro.kensait.spring.employee.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/*
 * Bearer のアクセストークンで社員 API を保護するリソースサーバーの設定
 * 検証の設定（公開鍵・受信者・アルゴリズム）は application.yml にある
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain api(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/realms/learn/protocol/openid-connect/token").permitAll() // 【1】
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.GET, "/employees", "/employees/**").hasAnyAuthority("BASIC", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/employees/**").hasAuthority("ADMIN") // 【2】
                .anyRequest().authenticated());
        http.oauth2ResourceServer(server -> server
                .jwt(jwt -> jwt.jwtAuthenticationConverter(employeeRolesConverter()))); // 【3】
        // 【4】 要求ごとにトークンで認証する。セッションも Cookie も使わないので CSRF トークンは要らない
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    // 【1】 発行者（iss）の検証を足す。この型の Bean は、自動構成の JwtDecoder の検証に加わる
    @Bean
    public OAuth2TokenValidator<Jwt> issuerValidator(@Value("${employee.jwt.expected-issuer}") String issuer) {
        return new JwtIssuerValidator(issuer);
    }

    // employee_roles クレームの値を、そのまま権限にする
    private JwtAuthenticationConverter employeeRolesConverter() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("employee_roles"); // 【2】 既定は scope／scp クレーム
        roles.setAuthorityPrefix(""); // 【3】 既定の接頭辞は SCOPE_
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(roles);
        converter.setPrincipalClaimName("preferred_username"); // 【4】 既定は sub
        return converter;
    }
}
