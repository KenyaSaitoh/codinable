package pro.kensait.jakartaee.employee.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;

/*
 * Faces のログイン画面のためのカスタムフォーム認証
 * 未ログインで保護された画面へ来たらログイン画面へ移し、ログインに成功したら元の画面へ戻す
 * （照合先のアイデンティティストアは IdentityStoreConfig）
 */
@CustomFormAuthenticationMechanismDefinition(
        loginToContinue = @LoginToContinue(
                loginPage = "/login.xhtml", // 【1】
                errorPage = "", // 【2】
                useForwardToLogin = false)) // 【3】
@ApplicationScoped
public class SecurityConfig {
}
