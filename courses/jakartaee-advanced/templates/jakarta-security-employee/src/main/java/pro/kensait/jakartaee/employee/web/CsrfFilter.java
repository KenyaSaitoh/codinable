package pro.kensait.jakartaee.employee.web;

import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;

/*
 * 変更要求（POST・PUT・DELETE）の X-CSRF-TOKEN ヘッダーを、セッションのトークンと照合する
 * 一致しなければ 403 を返し、リソースメソッドを実行しない
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class CsrfFilter implements ContainerRequestFilter {
    public static final String HEADER_NAME = "X-CSRF-TOKEN";
    public static final String TOKEN_ATTRIBUTE = "csrfToken";
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");
    @Context
    private HttpServletRequest request;

    @Override
    public void filter(ContainerRequestContext context) {
        if (SAFE_METHODS.contains(context.getMethod())) {
            return; // 【1】
        }
        HttpSession session = request.getSession(false);
        String expected = session == null ? null
                : (String) session.getAttribute(TOKEN_ATTRIBUTE); // 【2】
        String actual = context.getHeaderString(HEADER_NAME); // 【3】
        if (expected == null || actual == null || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8))) {
            context.abortWith(Response.status(Response.Status.FORBIDDEN).build()); // 【4】
        }
    }
}
