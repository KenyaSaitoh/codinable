package pro.kensait.leafbooks.web.login;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * トークンprocessorの機能を提供するクラス
 */
@Component
public class TokenProcessor {
    private static final Logger logger = LoggerFactory.getLogger(
            TokenProcessor.class);

    private final SecurityContextHolderStrategy securityContextHolderStrategy = 
            SecurityContextHolder.getContextHolderStrategy();

    @Autowired
    private SecurityContextRepository securityContextRepository;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    // 上の設定
    public void setUp(Object principal, Object credentials) {
        logger.info("[ TokenProcessor#setUp ]");
        // 認証済みのトークンを生成する
        List<GrantedAuthority> authorities = new ArrayList<>();
        Authentication token = UsernamePasswordAuthenticationToken
                .authenticated(principal, credentials, authorities);

        // SecurityContextを生成してトークンを設定
        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(token);

        // SecurityContextHolderに、認証済みトークンを登録する
        securityContextHolderStrategy.setContext(context);

        // Spring Security 6.x: SecurityContextRepositoryを使用してセッションに保存
        securityContextRepository.saveContext(context, request, response);
    }
}
