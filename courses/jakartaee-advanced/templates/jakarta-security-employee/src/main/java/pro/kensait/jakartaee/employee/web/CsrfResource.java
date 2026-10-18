package pro.kensait.jakartaee.employee.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

/*
 * CSRF トークンの発行（GET /api/csrf。認証を求めない）
 */
@Path("/csrf")
@RequestScoped
public class CsrfResource {
    @Context
    private HttpServletRequest request;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> issue() {
        HttpSession session = request.getSession(true); // 【1】
        String token = (String) session.getAttribute(CsrfFilter.TOKEN_ATTRIBUTE);
        if (token == null) {
            byte[] bytes = new byte[32];
            new SecureRandom().nextBytes(bytes); // 【2】
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(CsrfFilter.TOKEN_ATTRIBUTE, token); // 【3】
        }
        return Map.of("headerName", CsrfFilter.HEADER_NAME, "token", token); // 【4】
    }
}
