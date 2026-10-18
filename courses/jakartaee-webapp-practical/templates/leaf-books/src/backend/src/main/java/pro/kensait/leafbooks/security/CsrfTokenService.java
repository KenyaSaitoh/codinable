package pro.kensait.leafbooks.security;

import java.security.SecureRandom;
import java.util.Base64;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * CSRFトークンの発行
 * ログイン時に発行し、JavaScriptから読める Cookie（XSRF-TOKEN）として返す
 */
@ApplicationScoped
public class CsrfTokenService {
    public static final String COOKIE_NAME = "XSRF-TOKEN";
    public static final String HEADER_NAME = "X-XSRF-TOKEN";

    private final SecureRandom random = new SecureRandom();

    public String issue() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
