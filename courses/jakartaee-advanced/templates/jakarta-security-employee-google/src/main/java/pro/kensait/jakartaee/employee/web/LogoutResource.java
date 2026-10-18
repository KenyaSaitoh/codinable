package pro.kensait.jakartaee.employee.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

/*
 * ログアウト（POST /api/logout。204）
 * 認証状態を解除し、セッションを無効にする
 */
@Path("/logout")
@RequestScoped
public class LogoutResource {
    @Context
    private HttpServletRequest request;

    @POST
    public Response logout() throws ServletException {
        request.logout();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Response.noContent().build();
    }
}
