package pro.kensait.jpa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.persistence.Persistence;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import pro.kensait.course.SampleDatabase;
import pro.kensait.jpa.entity.Account;
import pro.kensait.jpa.entity.AccountPK;

/*
 * アカウント残高のテスト
 */
class AccountBalanceServiceTest {
    // 「参照時の残高不変性と二重計上防止」の検証
    @Test
    void readingDoesNotUpdateBalanceAndApplyingTwiceDoesNotDoubleCount() throws Exception {
        SampleDatabase.reset();
        try (var factory = Persistence.createEntityManagerFactory("MyPersistenceUnit");
                var entityManager = factory.createEntityManager()) {
            var transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                var account = entityManager.find(Account.class, new AccountPK(101, 10001));
                assertEquals(0, new BigDecimal("200000").compareTo(account.getBalance()));
                var service = new AccountBalanceService();
                service.applyPendingTransactions(entityManager, account);
                service.applyPendingTransactions(entityManager, account);
                transaction.commit();
                entityManager.clear();
                var stored = entityManager.find(Account.class, new AccountPK(101, 10001));
                assertEquals(0, new BigDecimal("205000").compareTo(stored.getBalance()));
            } finally {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
            }
        }
    }
}
