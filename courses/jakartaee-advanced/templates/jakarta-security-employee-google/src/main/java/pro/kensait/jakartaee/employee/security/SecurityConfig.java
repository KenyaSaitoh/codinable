package pro.kensait.jakartaee.employee.security;

import jakarta.annotation.security.DeclareRoles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.openid.ClaimsDefinition;

/*
 * Google を IdP とする OpenID Connect のログイン
 * Google の ID トークンには employee_roles も preferred_username も無いので、呼び出し元の名前には sub を使う
 * グループは BasicGroupIdentityStore が補う BASIC だけ（Google で認証されたという理由で ADMIN を与えない）
 */
@OpenIdAuthenticationMechanismDefinition(
        providerURI = "${oidcConfig.issuerUri}",
        clientId = "${oidcConfig.clientId}",
        clientSecret = "${oidcConfig.clientSecret}",
        redirectURI = "${baseURL}/callback",
        scope = {"openid", "profile", "email"},
        claimsDefinition = @ClaimsDefinition(callerNameClaim = "sub"),
        redirectToOriginalResource = true)
@DeclareRoles({"BASIC", "ADMIN"})
@ApplicationScoped
public class SecurityConfig {
}
