package pro.kensait.jakartaee.issuer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/*
 * 教材用の簡易な発行者（Keycloak の代わり）
 * 起動のたびに RSA の鍵ペアを作り、RS256 で署名した JWT を発行する。公開鍵は JWKS として公開する
 * 利用者は alice / 1111（BASIC）と bob / 2222（ADMIN）の 2 人だけ
 * 本物の認可サーバーではない（ログイン画面・認可コードフロー・リフレッシュトークンは無い）
 */
@ApplicationScoped
public class TokenIssuer {
    public static final String ISSUER = "http://localhost:8080/jakarta-mpjwt-issuer/realms/learn";
    private static final long LIFETIME_SECONDS = 300;

    private record User(String password, String subject, String email, List<String> roles) {
    }

    private static final Map<String, User> USERS = Map.of(
            "alice", new User("1111", "8f2d6c1e-0a01-4c5e-9a10-000000010001", "alice@example.test", List.of("BASIC")),
            "bob", new User("2222", "8f2d6c1e-0a01-4c5e-9a10-000000010002", "bob@example.test", List.of("ADMIN")));

    private final KeyPair keyPair;
    private final String kid = UUID.randomUUID().toString();

    public TokenIssuer() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            keyPair = generator.generateKeyPair();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /** 公開鍵の集合（JWKS） */
    public JsonObject jwks() {
        RSAPublicKey key = (RSAPublicKey) keyPair.getPublic();
        return Json.createObjectBuilder().add("keys", Json.createArrayBuilder().add(Json.createObjectBuilder()
                .add("kty", "RSA").add("kid", kid).add("use", "sig").add("alg", "RS256")
                .add("n", base64Url(unsigned(key.getModulus())))
                .add("e", base64Url(unsigned(key.getPublicExponent()))))).build();
    }

    /** パスワードを照合し、アクセストークンと ID トークンを返す。照合できなければ null */
    public JsonObject issue(String username, String password) {
        User user = USERS.get(username);
        if (user == null || !user.password().equals(password)) {
            return null;
        }
        long now = Instant.now().getEpochSecond();
        JsonObject access = claims(username, user, now)
                .add("typ", "Bearer")
                .add("aud", Json.createArrayBuilder().add("employee-api").add("account"))
                .add("azp", "employee-app")
                .add("groups", Json.createArrayBuilder(user.roles()))
                .add("employee_roles", Json.createArrayBuilder(user.roles()))
                .build();
        JsonObject id = claims(username, user, now)
                .add("typ", "ID")
                .add("aud", "employee-app")
                .add("email", user.email())
                .add("employee_roles", Json.createArrayBuilder(user.roles()))
                .build();
        return Json.createObjectBuilder()
                .add("access_token", sign(access))
                .add("id_token", sign(id))
                .add("token_type", "Bearer")
                .add("expires_in", LIFETIME_SECONDS)
                .build();
    }

    private JsonObjectBuilder claims(String username, User user, long now) {
        return Json.createObjectBuilder()
                .add("iss", ISSUER)
                .add("sub", user.subject())
                .add("preferred_username", username)
                .add("iat", now)
                .add("exp", now + LIFETIME_SECONDS)
                .add("jti", UUID.randomUUID().toString());
    }

    private String sign(JsonObject payload) {
        String header = Json.createObjectBuilder()
                .add("alg", "RS256").add("typ", "JWT").add("kid", kid).build().toString();
        String input = base64Url(header.getBytes(StandardCharsets.UTF_8)) + "."
                + base64Url(payload.toString().getBytes(StandardCharsets.UTF_8));
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(keyPair.getPrivate());
            signature.update(input.getBytes(StandardCharsets.US_ASCII));
            return input + "." + base64Url(signature.sign());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] unsigned(BigInteger value) {
        byte[] bytes = value.toByteArray();
        return bytes[0] == 0 ? java.util.Arrays.copyOfRange(bytes, 1, bytes.length) : bytes;
    }

    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
