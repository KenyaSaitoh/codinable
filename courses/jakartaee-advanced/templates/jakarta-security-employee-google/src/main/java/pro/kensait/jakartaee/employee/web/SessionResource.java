package pro.kensait.jakartaee.employee.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.identitystore.openid.OpenIdContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

/*
 * コンテナが確定した呼び出し元と、IdP の認証結果を返す（GET /api/session）
 * Google 版では、呼び出し元の名前は sub の値になる。トークンの全文は返さない
 */
@Path("/session")
@RequestScoped
public class SessionResource {
    @Inject
    private SecurityContext securityContext;
    @Inject
    private OpenIdContext openIdContext;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> session() {
        return Map.of(
                "name", securityContext.getCallerPrincipal().getName(),
                "subject", openIdContext.getSubject(),
                "email", openIdContext.getClaims().getEmail().orElse(""),
                "roles", securityContext.getAllDeclaredCallerRoles());
    }
}
