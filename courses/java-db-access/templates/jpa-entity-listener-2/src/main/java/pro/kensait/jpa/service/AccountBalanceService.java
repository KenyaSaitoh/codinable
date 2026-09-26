package pro.kensait.jpa.service;

import jakarta.persistence.EntityManager;
import pro.kensait.jpa.entity.Account;
import pro.kensait.jpa.entity.TranDetail;
/** 未反映明細を反映する呼び出し元がトランザクションを開始・確定する */
public class AccountBalanceService {
    // pendingtransactionsの適用
    public void applyPendingTransactions(EntityManager entityManager, Account account) {
        if (!entityManager.getTransaction().isActive()) {
            throw new IllegalStateException("An active transaction is required");
        }
        var details = entityManager.createQuery("""
                select t from TranDetail t
                where t.id.branchNum = :branch and t.id.accountNum = :account
                  and (t.id.tranDate > :date
                       or (t.id.tranDate = :date and t.id.tranNum > :number))
                order by t.id.tranDate, t.id.tranNum
                """, TranDetail.class)
                .setParameter("branch", account.getId().getBranchNum())
                .setParameter("account", account.getId().getAccountNum())
                .setParameter("date", account.getLastTranDate())
                .setParameter("number", account.getLastTranNum()).getResultList();
        for (TranDetail detail : details) {
            var balance = switch (detail.getPayRecType()) {
                case PAY -> account.getBalance().subtract(detail.getAmount());
                case REC -> account.getBalance().add(detail.getAmount());
            };
            account.setBalance(balance);
            account.setLastTranDate(detail.getId().getTranDate());
            account.setLastTranNum(detail.getId().getTranNum());
        }
    }
}
