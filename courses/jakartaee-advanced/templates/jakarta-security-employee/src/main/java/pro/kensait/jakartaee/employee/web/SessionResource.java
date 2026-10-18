package pro.kensait.jakartaee.employee.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/*
 * 認証済みの呼び出し元と、そのロールを返す（GET /api/session）
 * 利用者名は要求の JSON やクエリからではなく、コンテナが登録した呼び出し元から取る
 */
@Path("/session")
@RequestScoped
public class SessionResource {
    @Inject
    private SecurityContext securityContext;

    public record SessionView(String username, List<String> roles) {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SessionView session() {
        // Payara の場合、web.xml の auth-constraint に書いた「**」（認証済みなら誰でも）もロールの一覧に入るので除く
        return new SessionView(securityContext.getCallerPrincipal().getName(),
                securityContext.getAllDeclaredCallerRoles().stream()
                        .filter(role -> !"**".equals(role)).sorted().toList());
    }
}
