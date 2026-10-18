package pro.kensait.leafbooks.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import pro.kensait.leafbooks.api.dto.ErrorResponse;

/**
 * JWT認証フィルター
 * CookieからJWTを抽出・検証し、AuthContextに認証情報を設定する
 * 公開パス（isPublicPath）以外は、JWTがなければ 401 で拒否する（既定で拒否）
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Inject
    private JwtUtil jwtUtil;

    @Inject
    private AuthContext authContext;

    @Context
    private HttpServletRequest httpServletRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String requestUri = requestContext.getUriInfo().getPath();
        logger.debug("[ JwtAuthFilter#filter ] URI: {}", requestUri);

        // 認証不要なパス（公開API）
        if (isPublicPath(requestUri)) {
            logger.debug("[ JwtAuthFilter ] Public path, skip authentication");
            return;
        }

        try {
            // CookieからJWTを抽出
            String jwt = jwtUtil.extractJwtFromRequest(httpServletRequest);

            if (jwt != null && jwtUtil.validateToken(jwt)) {
                // JWTから顧客情報を取得し、AuthContextに設定
                authContext.setCustomerId(jwtUtil.getCustomerIdFromToken(jwt));
                authContext.setEmail(jwtUtil.getEmailFromToken(jwt));
                logger.debug("[ JwtAuthFilter ] Authenticated customerId: {}", authContext.getCustomerId());
            } else {
                logger.warn("[ JwtAuthFilter ] Unauthorized access to: {}", requestUri);
                abortUnauthorized(requestContext, "認証が必要です", requestUri);
            }
        } catch (Exception e) {
            logger.error("[ JwtAuthFilter ] Authentication error: {}", e.getMessage());
            abortUnauthorized(requestContext, "認証が必要です", requestUri);
        }
    }

    private void abortUnauthorized(ContainerRequestContext requestContext, String message, String path) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(ErrorResponse.unauthorized(message, path))
                        .build());
    }

    /**
     * 公開パス（認証不要）かどうかを判定する
     * ここに列挙したパス以外は、すべて認証必須として扱う
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("auth/login")
                || path.startsWith("auth/register")
                || path.startsWith("auth/logout")
                || path.startsWith("books")
                || path.startsWith("categories")
                || path.startsWith("publishers")
                || path.startsWith("images");
    }
}
