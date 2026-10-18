package pro.kensait.leafbooks.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * 社員認証用 JWT ユーティリティ
 * 顧客認証の JwtUtil と設定キーを分けて管理する
 */
@ApplicationScoped
public class EmployeeJwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeJwtUtil.class);

    @Inject
    @ConfigProperty(name = "employee.jwt.secret-key")
    private String secretKeyString;

    @Inject
    @ConfigProperty(name = "employee.jwt.expiration-ms")
    private Long expirationMs;

    @Inject
    @ConfigProperty(name = "employee.jwt.cookie-name")
    private String cookieName;

    public String generateToken(Long employeeId, String employeeCode, Long departmentId) {
        logger.info("[ EmployeeJwtUtil#generateToken ] employeeId: {}", employeeId);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);
        SecretKey key = Keys.hmacShaKeyFor(secretKeyString.getBytes());
        return Jwts.builder()
                .subject(String.valueOf(employeeId))
                .claim("employeeCode", employeeCode)
                .claim("departmentId", departmentId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Claims getClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKeyString.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            logger.warn("[ EmployeeJwtUtil#validateToken ] Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    public String getCookieName() { return cookieName; }

    public int getExpirationSeconds() { return (int) (expirationMs / 1000); }
}
