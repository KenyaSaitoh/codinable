package pro.kensait.spring.employee.web;
import java.util.HashSet;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
/*
 * 社員oidcユーザー機能のビジネスロジック
 */
public class EmployeeOidcUserService extends OidcUserService {
    // サービスメソッド：OIDCプロバイダからユーザー情報の取得
    @Override
    public OidcUser loadUser(OidcUserRequest request) {
        OidcUser user = java.util.Objects.requireNonNull(super.loadUser(request), "OIDC user is required");
        var authorities = new HashSet<GrantedAuthority>(user.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("BASIC"));
        // Googleログインだけで管理権限は与えない全ユーザーが閲覧専用
        return new DefaultOidcUser(authorities, user.getIdToken(), user.getUserInfo(), "sub");
    }
}
