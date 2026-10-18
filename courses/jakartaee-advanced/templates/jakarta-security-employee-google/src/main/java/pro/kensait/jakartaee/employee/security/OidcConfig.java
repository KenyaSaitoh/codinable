package pro.kensait.jakartaee.employee.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/*
 * Google の OIDC のクライアントの設定値を MicroProfile Config から読み、式言語へ oidcConfig という名前で渡す
 * クライアントIDとシークレットは、Google Cloud のコンソールで登録した Web アプリケーションの値
 * （環境変数 GOOGLE_CLIENT_ID・GOOGLE_CLIENT_SECRET、または payara/post-boot-commands.asadmin で与える）
 * 未設定でもデプロイはできるよう、既定値は「未設定」を表す文字列にしている（その場合、ログインは Google が拒否する）
 */
@Named("oidcConfig")
@ApplicationScoped
public class OidcConfig {
    @Inject
    @ConfigProperty(name = "google.issuer-uri", defaultValue = "https://accounts.google.com")
    private String issuerUri;
    @Inject
    @ConfigProperty(name = "google.client-id", defaultValue = "not-configured")
    private String clientId;
    @Inject
    @ConfigProperty(name = "google.client-secret", defaultValue = "not-configured")
    private String clientSecret;

    public String getIssuerUri() { return issuerUri; }
    public String getClientId() { return clientId; }
    public String getClientSecret() { return clientSecret; }
}
