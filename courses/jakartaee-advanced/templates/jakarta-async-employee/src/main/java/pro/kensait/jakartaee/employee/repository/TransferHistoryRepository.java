package pro.kensait.jakartaee.employee.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import pro.kensait.jakartaee.employee.entity.TransferHistory;

/*
 * 異動履歴の読み書き
 */
@ApplicationScoped
public class TransferHistoryRepository {
    @PersistenceContext(unitName = "employeePU")
    private EntityManager em;

    public void save(TransferHistory history) {
        em.persist(history);
    }

    public List<TransferHistory> findAll() {
        return em.createQuery("SELECT h FROM TransferHistory h ORDER BY h.historyId", TransferHistory.class)
                .getResultList();
    }
}
