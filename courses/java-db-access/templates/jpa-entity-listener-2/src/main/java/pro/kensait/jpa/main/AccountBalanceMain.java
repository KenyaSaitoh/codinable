package pro.kensait.jpa.main;

import jakarta.persistence.Persistence;
import pro.kensait.jpa.entity.Account;
import pro.kensait.jpa.entity.AccountPK;
import pro.kensait.jpa.service.AccountBalanceService;

/*
 * アカウント残高の機能を提供するクラス
 */
public class AccountBalanceMain {
    public static void main(String[] args) {
        try (var factory = Persistence.createEntityManagerFactory("MyPersistenceUnit");
                var entityManager = factory.createEntityManager()) {
            var transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                var account = entityManager.find(Account.class, new AccountPK(101, 10001));
                new AccountBalanceService().applyPendingTransactions(entityManager, account);
                transaction.commit();
                System.out.println(account);
            } finally {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
            }
        }
    }
}
