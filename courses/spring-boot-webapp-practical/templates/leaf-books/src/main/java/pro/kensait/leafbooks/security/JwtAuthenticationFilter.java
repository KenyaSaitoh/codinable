package pro.kensait.leafbooks.security;

import java.io.IOException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * jwtauthenticationの機能を提供するクラス
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    // doフィルターinternalの実行
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        logger.debug("[ JwtAuthenticationFilter#doFilterInternal ] URI: {}", request.getRequestURI());

        try {
            // CookieからJWTを抽出
            String jwt = jwtUtil.extractJwtFromRequest(request);

            if (jwt != null && jwtUtil.validateToken(jwt)) {
                // JWTから顧客情報を取得
                Integer customerId = jwtUtil.getCustomerIdFromToken(jwt);
                String email = jwtUtil.getEmailFromToken(jwt);

                // 認証トークンを生成し、SecurityContextに設定
                JwtAuthenticationToken authentication = 
                        new JwtAuthenticationToken(customerId, email, Collections.emptyList());
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                logger.debug("[ JwtAuthenticationFilter ] Authenticated customerId: {}, email: {}", 
                        customerId, email);
            }
        } catch (Exception e) {
            logger.error("[ JwtAuthenticationFilter ] Could not set user authentication: {}", 
                    e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}

