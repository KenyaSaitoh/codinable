package pro.kensait.leafbooks.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import pro.kensait.leafbooks.api.dto.ErrorResponse;

/**
 * CSRFトークン検証フィルター
 * 更新系（POST / PUT / DELETE / PATCH）の要求で、Cookie XSRF-TOKEN の値と
 * ヘッダー X-XSRF-TOKEN の値が一致することを確かめ、一致しなければ 403 を返す
 * ログインと新規登録は、トークンを受け取る前の要求なので対象外
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class CsrfTokenFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(CsrfTokenFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        if (!("POST".equals(method) || "PUT".equals(method)
                || "DELETE".equals(method) || "PATCH".equals(method))) {
            return;
        }
        String requestUri = requestContext.getUriInfo().getPath();
        if (requestUri.startsWith("auth/login") || requestUri.startsWith("auth/register")) {
            return;
        }

        Cookie cookie = requestContext.getCookies().get(CsrfTokenService.COOKIE_NAME);
        String cookieValue = (cookie != null) ? cookie.getValue() : null;
        String headerValue = requestContext.getHeaderString(CsrfTokenService.HEADER_NAME);

        if (cookieValue == null || cookieValue.isEmpty() || !cookieValue.equals(headerValue)) {
            logger.warn("[ CsrfTokenFilter ] CSRF token mismatch: {} {}", method, requestUri);
            requestContext.abortWith(
                    Response.status(Response.Status.FORBIDDEN)
                            .type(MediaType.APPLICATION_JSON)
                            .entity(ErrorResponse.forbidden("CSRFトークンが正しくありません", requestUri))
                            .build());
        }
    }
}
