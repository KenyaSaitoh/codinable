package pro.kensait.spring.calc.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/*
 * CustomOidcUserService の「クレーム → アプリケーション権限」の変換を、Cognito に接続せずに確かめるテスト
 * （Codinable 版で追加したもの。講座リポジトリのサンプルには含まれない）
 *
 * ClientRegistration に UserInfo エンドポイントを設定しないので、標準の OidcUserService は
 * ネットワークへ出ず、渡した ID トークンのクレームだけで OidcUser を組み立てる
 * ここで使う clientId や URL はテスト専用の値で、実在の Cognito とは関係しない
 */
class CustomOidcUserServiceTest {

    private final CustomOidcUserService service = new CustomOidcUserService();

    // cognito:groups クレームを持つ（または持たない）ログイン結果を作る
    private OidcUserRequest requestWithGroups(List<String> groups) {
        ClientRegistration registration = ClientRegistration.withRegistrationId("cognito")
                .clientId("test-client")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri("https://idp.example.test/oauth2/authorize")
                .tokenUri("https://idp.example.test/oauth2/token")
                .build();
        Instant now = Instant.now();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                "access-token", now, now.plusSeconds(300), Set.of("openid", "profile", "email"));
        OidcIdToken.Builder idToken = OidcIdToken.withTokenValue("id-token")
                .subject("user-sub")
                .claim("username", "alice")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300));
        if (groups != null) {
            idToken.claim("cognito:groups", groups);
        }
        return new OidcUserRequest(registration, accessToken, idToken.build());
    }

    private boolean hasAdmin(OidcUser user) {
        return user.getAuthorities().stream()
                .anyMatch(a -> "ADMIN".equals(a.getAuthority()));
    }


    @Test
    void cognito1ではグループに関係なくADMINが付く() {
        assertTrue(hasAdmin(service.loadUser(requestWithGroups(null))));
        assertTrue(hasAdmin(service.loadUser(requestWithGroups(List.of("USER")))));
    }

    @Test
    void 元のOIDC権限も引き継ぐ() {
        OidcUser user = service.loadUser(requestWithGroups(null));
        assertTrue(user.getAuthorities().stream()
                .anyMatch(a -> "OIDC_USER".equals(a.getAuthority())));
    }
}
