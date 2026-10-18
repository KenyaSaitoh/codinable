package pro.kensait.jakartaee.employee.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import java.util.EnumSet;
import java.util.Set;

/*
 * OIDC の認証に成功した呼び出し元に BASIC グループを補うアイデンティティストア
 * 資格情報の照合はしない（PROVIDE_GROUPS だけ）。Google 版のグループは BASIC だけになる
 */
@ApplicationScoped
public class BasicGroupIdentityStore implements IdentityStore {
    @Override
    public Set<String> getCallerGroups(CredentialValidationResult result) {
        return Set.of("BASIC");
    }

    @Override
    public Set<ValidationType> validationTypes() {
        return EnumSet.of(ValidationType.PROVIDE_GROUPS);
    }
}
