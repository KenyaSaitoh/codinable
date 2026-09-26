package pro.kensait.spring.calc.web;

import java.util.HashSet;
import java.util.List;
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

        // Cognitoのグループ情報（"cognito:groups"クレーム）を取得し、権限にマッピングする
        // * cognito-1では便宜上、全ユーザーに"ADMIN"を固定付与していたが、
        //   ここではIdP（Cognito）が管理するグループ情報から権限を決定する
        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());

        // IDトークンのクレームから"cognito:groups"（所属グループ名のリスト）を取得する
        List<String> groups = oidcUser.getClaimAsStringList("cognito:groups");
        if (groups != null && groups.contains("ADMIN")) {
            // "ADMIN"グループに所属するユーザーには"ADMIN"権限を付与する
            authorities.add(new SimpleGrantedAuthority("ADMIN"));
        }

        return new DefaultOidcUser(authorities,
                oidcUser.getIdToken(),
                oidcUser.getUserInfo());
    }
}
