package pro.kensait.leafbooks.security;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/*
 * jwtauthenticationトークンの機能を提供するクラス
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final Integer customerId;
    private final String email;

    // jwtauthenticationトークンの初期化
    public JwtAuthenticationToken(Integer customerId, String email,
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.customerId = customerId;
        this.email = email;
        setAuthenticated(true);
    }

    // credentialsの取得
    @Override
    public Object getCredentials() {
        return null;
    }

    // principalの取得
    @Override
    public Object getPrincipal() {
        return customerId;
    }

    // 顧客IDの取得
    public Integer getCustomerId() {
        return customerId;
    }

    // メールアドレスの取得
    public String getEmail() {
        return email;
    }
}

