package pro.kensait.spring.employee.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import pro.kensait.spring.employee.issuer.TokenIssuer;

/*
 * トークンの検証（401）と権限の判定（403）を、実際の HTTP で確かめる
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmployeeApiSecurityTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TokenIssuer issuer;
    @Value("classpath:keys/issuer-public.pem")
    private RSAPublicKey publicKey;
    @Value("classpath:keys/issuer-private.pem")
    private RSAPrivateKey privateKey;

    private RestClient client;

    @BeforeEach
    void connect() {
        client = RestClient.create("http://127.0.0.1:" + port);
    }

    // トークンなしは 401。どの形式を要求しているかを WWW-Authenticate で返す
    @Test
    void noTokenIs401() {
        Result result = call(HttpMethod.GET, "/employees", null);
        assertEquals(401, result.status());
        assertTrue(result.wwwAuthenticate().startsWith("Bearer"), result.wwwAuthenticate());
    }

    // BASIC は参照できる
    @Test
    void basicCanRead() {
        assertEquals(200, call(HttpMethod.GET, "/employees", accessToken("alice", "1111")).status());
    }

    // BASIC は削除できない。トークンは有効なので 401 ではなく 403
    @Test
    void basicCannotDelete() {
        Result result = call(HttpMethod.DELETE, "/employees/10015", accessToken("alice", "1111"));
        assertEquals(403, result.status());
        assertTrue(result.wwwAuthenticate().contains("insufficient_scope"), result.wwwAuthenticate());
    }

    // ADMIN は削除できる。2 回目は社員が無いので 404（認可は通っている）
    @Test
    void adminCanDelete() {
        String bob = accessToken("bob", "2222");
        assertEquals(200, call(HttpMethod.GET, "/employees", bob).status());
        assertEquals(204, call(HttpMethod.DELETE, "/employees/10016", bob).status());
        assertEquals(404, call(HttpMethod.DELETE, "/employees/10016", bob).status());
    }

    // ID トークンは署名も発行者も正しいが、aud に employee-api が無いので受け入れない
    @Test
    void idTokenIsRejected() {
        String idToken = (String) issuer.issue("bob", "2222").get("id_token");
        assertEquals(401, call(HttpMethod.GET, "/employees", idToken).status());
    }

    // 署名を書き換えたトークンは受け入れない
    @Test
    void tamperedSignatureIsRejected() {
        String token = accessToken("bob", "2222");
        String tampered = token.substring(0, token.length() - 5) + "AAAAA";
        assertEquals(401, call(HttpMethod.GET, "/employees", tampered).status());
    }

    // 別の鍵で署名したトークンは、中身が正しくても受け入れない
    @Test
    void otherKeyIsRejected() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair other = generator.generateKeyPair();
        String token = sign((RSAPublicKey) other.getPublic(), (RSAPrivateKey) other.getPrivate(),
                validClaims().build());
        assertEquals(401, call(HttpMethod.GET, "/employees", token).status());
    }

    // 発行者が違うトークンは受け入れない
    @Test
    void otherIssuerIsRejected() {
        String token = sign(publicKey, privateKey, validClaims().issuer("http://localhost:9999/realms/other").build());
        assertEquals(401, call(HttpMethod.GET, "/employees", token).status());
    }

    // 期限切れのトークンは受け入れない（時刻のずれの許容の 60 秒を超えたもの）
    @Test
    void expiredTokenIsRejected() {
        Instant past = Instant.now().minusSeconds(600);
        String token = sign(publicKey, privateKey,
                validClaims().issuedAt(past).expiresAt(past.plusSeconds(300)).build());
        assertEquals(401, call(HttpMethod.GET, "/employees", token).status());
    }

    // 同じ組み立てで正しいものは通る。上の拒否が、確かめたい項目以外の理由で起きていないことを押さえる
    @Test
    void validHandMadeTokenIsAccepted() {
        assertEquals(200, call(HttpMethod.GET, "/employees",
                sign(publicKey, privateKey, validClaims().build())).status());
    }

    // パスワードが違えば発行しない
    @Test
    void wrongPasswordGetsNoToken() {
        assertEquals(401, tokenRequest("alice", "wrong"));
    }

    private String accessToken(String username, String password) {
        return (String) issuer.issue(username, password).get("access_token");
    }

    private JwtClaimsSet.Builder validClaims() {
        Instant now = Instant.now();
        return JwtClaimsSet.builder()
                .issuer(TokenIssuer.ISSUER)
                .subject("test")
                .audience(List.of("employee-api"))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .claim("preferred_username", "test")
                .claim("employee_roles", List.of("ADMIN"));
    }

    private String sign(RSAPublicKey pub, RSAPrivateKey priv, JwtClaimsSet claims) {
        RSAKey key = new RSAKey.Builder(pub).privateKey(priv).keyID("test").build();
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key)));
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256).keyId("test").build(), claims)).getTokenValue();
    }

    private record Result(int status, String wwwAuthenticate) {
    }

    private Result call(HttpMethod method, String path, String bearer) {
        return client.method(method).uri(path)
                .headers(headers -> {
                    if (bearer != null) {
                        headers.setBearerAuth(bearer);
                    }
                })
                .exchange((request, response) -> new Result(response.getStatusCode().value(),
                        response.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE)));
    }

    private int tokenRequest(String username, String password) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("username", username);
        form.add("password", password);
        return client.post().uri("/realms/learn/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .exchange((request, response) -> response.getStatusCode().value());
    }
}
