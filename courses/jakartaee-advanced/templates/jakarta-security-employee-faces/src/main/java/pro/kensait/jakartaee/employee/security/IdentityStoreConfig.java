package pro.kensait.jakartaee.employee.security;

import jakarta.annotation.security.DeclareRoles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

/*
 * データベースの利用者情報（APP_USER・APP_USER_GROUP）で照合するアイデンティティストア
 * jakarta-security-employee の SecurityConfig と同じ設定（ログイン画面の方式を変えても照合先は変わらない）
 */
@DatabaseIdentityStoreDefinition(
        dataSourceLookup = "java:comp/DefaultDataSource",
        callerQuery = "SELECT PASSWORD_HASH FROM APP_USER WHERE USER_NAME = ?",
        groupsQuery = "SELECT GROUP_NAME FROM APP_USER_GROUP WHERE USER_NAME = ?",
        hashAlgorithm = Pbkdf2PasswordHash.class,
        hashAlgorithmParameters = {
                "Pbkdf2PasswordHash.Algorithm=PBKDF2WithHmacSHA256",
                "Pbkdf2PasswordHash.Iterations=600000",
                "Pbkdf2PasswordHash.SaltSizeBytes=32",
                "Pbkdf2PasswordHash.KeySizeBytes=32"})
@DeclareRoles({"BASIC", "ADMIN"})
@ApplicationScoped
public class IdentityStoreConfig {
}
