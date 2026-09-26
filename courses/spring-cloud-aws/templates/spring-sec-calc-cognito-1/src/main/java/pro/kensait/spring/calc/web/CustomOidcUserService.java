package pro.kensait.spring.calc.web;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/*
 * OpenID Connect (OIDC) 認証プロセスを表すクラス
 */
public class CustomOidcUserService extends OidcUserService {
    private static final Logger logger = LoggerFactory.getLogger(
            CustomOidcUserService.class);

    // サービスメソッド：OIDCプロバイダからユーザー情報の取得
    @Override
    public OidcUser loadUser(OidcUserRequest request) {
        OidcUser oidcUser = super.loadUser(request);
        logger.info("[ CustomOidcUserService#loadUser ] oidcUser => " + oidcUser);

        // * Cognitoで認証済みのユーザーから、何らかの方法で属性を取得し、ロールを設定する
        // * ここでは便宜上、すべてのユーザーに"ADMIN"権限を設定
        //   （Cognitoのグループ情報から権限を決定する方法は、cognito-2を参照）
        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("ADMIN"));

        return new DefaultOidcUser(authorities,
                oidcUser.getIdToken(),
                oidcUser.getUserInfo());
    }
}
