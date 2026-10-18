package pro.kensait.jakartaee.employee.security;

import jakarta.annotation.security.DeclareRoles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;

/*
 * データベースの利用者情報（APP_USER・APP_USER_GROUP）で照合するアイデンティティストア
 * java:comp/DefaultDataSource は、Payara Server が既定で持つ H2 のデータベース
 */
@DatabaseIdentityStoreDefinition(
        dataSourceLookup = "java:comp/DefaultDataSource", // 【1】
        callerQuery = "SELECT PASSWORD_HASH FROM APP_USER WHERE USER_NAME = ?", // 【2】
        groupsQuery = "SELECT GROUP_NAME FROM APP_USER_GROUP WHERE USER_NAME = ?", // 【3】
        hashAlgorithm = Pbkdf2PasswordHash.class, // 【4】
        hashAlgorithmParameters = {
                "Pbkdf2PasswordHash.Algorithm=PBKDF2WithHmacSHA256",
                "Pbkdf2PasswordHash.Iterations=600000", // 【5】
                "Pbkdf2PasswordHash.SaltSizeBytes=32",
                "Pbkdf2PasswordHash.KeySizeBytes=32"})
@DeclareRoles({"BASIC", "ADMIN"}) // 【6】
@ApplicationScoped
public class SecurityConfig {
}
