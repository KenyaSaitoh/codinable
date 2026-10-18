package pro.kensait.leafbooks.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import pro.kensait.leafbooks.entity.Stock;

@ApplicationScoped
public class StockDao {
    private static final Logger logger = LoggerFactory.getLogger(StockDao.class);

    @PersistenceContext(unitName = "bookstorePU")
    private EntityManager em;

    public Stock findByIdWithLock(Integer bookId) {
        logger.info("[ StockDao#findByIdWithLock ]");
        return em.find(Stock.class, bookId, LockModeType.PESSIMISTIC_WRITE);
    }

    public Stock findById(Integer bookId) {
        logger.info("[ StockDao#findById ]");
        return em.find(Stock.class, bookId);
    }

    public List<Stock> findAll() {
        logger.info("[ StockDao#findAll ]");
        return em.createQuery("SELECT s FROM Stock s ORDER BY s.bookId", Stock.class)
                .getResultList();
    }

    public void update(Stock stock) {
        logger.info("[ StockDao#update ]");
        em.merge(stock);
    }
}
