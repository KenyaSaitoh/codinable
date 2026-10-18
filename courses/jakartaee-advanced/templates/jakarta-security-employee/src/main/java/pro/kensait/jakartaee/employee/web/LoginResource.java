package pro.kensait.jakartaee.employee.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/*
 * ログイン（POST /api/login。成功は 204、失敗は 401）
 * Jakarta REST の SecurityContext ではなく、jakarta.security.enterprise.SecurityContext を使う
 */
@Path("/login")
@RequestScoped
public class LoginResource {
    @Inject
    private SecurityContext securityContext;
    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(@FormParam("username") String username,
            @FormParam("password") String password) {
        var credential = new UsernamePasswordCredential(username, password); // 【1】
        AuthenticationStatus status = securityContext.authenticate(request, response,
                AuthenticationParameters.withParams()
                        .credential(credential).newAuthentication(true)); // 【2】
        if (status != AuthenticationStatus.SUCCESS) {
            return Response.status(Response.Status.UNAUTHORIZED).build(); // 【3】
        }
        request.getSession().removeAttribute(CsrfFilter.TOKEN_ATTRIBUTE); // 【4】
        return Response.noContent().build();
    }
}
