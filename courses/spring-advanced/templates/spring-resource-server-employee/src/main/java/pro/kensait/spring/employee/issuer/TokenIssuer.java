package pro.kensait.spring.employee.issuer;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

/*
 * 教材用の簡易な発行者（Keycloak の代わり）
 * リソースに置いた RSA の秘密鍵で、RS256 で署名した JWT を発行する
 * 利用者は alice / 1111（BASIC）と bob / 2222（ADMIN）の 2 人だけ
 * 本物の認可サーバーではない（ログイン画面・認可コードフロー・リフレッシュトークンは無い）
 * 社員 API 側（web パッケージ）はこのクラスを参照しない。公開鍵だけでトークンを検証している
 */
@Component
public class TokenIssuer {
    public static final String ISSUER = "http://localhost:8097/realms/learn";
    private static final String KEY_ID = "learn-teaching-key";
    private static final long LIFETIME_SECONDS = 300;

    private record User(String password, String subject, String email, List<String> roles) {
    }

    private static final Map<String, User> USERS = Map.of(
            "alice", new User("1111", "8f2d6c1e-0a01-4c5e-9a10-000000010001", "alice@example.com", List.of("BASIC")),
            "bob", new User("2222", "8f2d6c1e-0a01-4c5e-9a10-000000010002", "bob@example.com", List.of("ADMIN")));

    private final JwtEncoder encoder;

    public TokenIssuer(@Value("classpath:keys/issuer-public.pem") RSAPublicKey publicKey,
            @Value("classpath:keys/issuer-private.pem") RSAPrivateKey privateKey) {
        RSAKey key = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(KEY_ID).build();
        this.encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key)));
    }

    /** パスワードを照合し、アクセストークンと ID トークンを返す。照合できなければ null */
    public Map<String, Object> issue(String username, String password) {
        User user = USERS.get(username);
        if (user == null || !user.password().equals(password)) {
            return null;
        }
        Instant now = Instant.now();
        JwtClaimsSet access = claims(username, user, now)
                .claim("typ", "Bearer")
                .audience(List.of("employee-api", "account"))
                .claim("azp", "employee-app")
                .claim("employee_roles", user.roles())
                .build();
        JwtClaimsSet id = claims(username, user, now)
                .claim("typ", "ID")
                .audience(List.of("employee-app"))
                .claim("email", user.email())
                .claim("employee_roles", user.roles())
                .build();
        return Map.of("access_token", sign(access), "id_token", sign(id),
                "token_type", "Bearer", "expires_in", LIFETIME_SECONDS);
    }

    private JwtClaimsSet.Builder claims(String username, User user, Instant now) {
        return JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(user.subject())
                .claim("preferred_username", username)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(LIFETIME_SECONDS))
                .id(UUID.randomUUID().toString());
    }

    private String sign(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId(KEY_ID).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
