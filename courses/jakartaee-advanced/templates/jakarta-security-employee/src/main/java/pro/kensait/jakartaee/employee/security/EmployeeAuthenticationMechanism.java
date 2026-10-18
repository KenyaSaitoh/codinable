package pro.kensait.jakartaee.employee.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.AutoApplySession;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * REST API のための独自の認証メカニズム
 * 未ログインで保護された URL へ来たら、ログイン画面へ転送せずに 401 を返す
 * 資格情報は LoginResource が SecurityContext.authenticate で渡す
 */
@AutoApplySession // 【1】
@ApplicationScoped
public class EmployeeAuthenticationMechanism implements HttpAuthenticationMechanism {
    @Inject
    private IdentityStoreHandler identityStoreHandler;

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request,
            HttpServletResponse response, HttpMessageContext context) {
        Credential credential = context.getAuthParameters().getCredential(); // 【2】
        if (credential != null) {
            CredentialValidationResult result = identityStoreHandler.validate(credential); // 【3】
            if (result.getStatus() == CredentialValidationResult.Status.VALID
                    && request.getSession(false) != null) {
                request.changeSessionId(); // 【4】
            }
            return context.notifyContainerAboutLogin(result); // 【5】
        }
        if (context.isProtected()) {
            return context.responseUnauthorized(); // 【6】
        }
        return context.doNothing();
    }
}
