package pro.kensait.spring.employee.issuer;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
 * 教材用の発行者のトークンエンドポイント（Keycloak と同じ形のパス）
 * パスワードグラントだけを受け付ける（検証用の近道）
 */
@RestController
public class TokenEndpoint {
    private final TokenIssuer issuer;

    public TokenEndpoint(TokenIssuer issuer) {
        this.issuer = issuer;
    }

    @PostMapping(path = "/realms/learn/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> token(@RequestParam("grant_type") String grantType,
            @RequestParam String username, @RequestParam String password) {
        if (!"password".equals(grantType)) {
            return ResponseEntity.badRequest().body(Map.of("error", "unsupported_grant_type"));
        }
        Map<String, Object> tokens = issuer.issue(username, password);
        if (tokens == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid_grant"));
        }
        return ResponseEntity.ok(tokens);
    }
}
