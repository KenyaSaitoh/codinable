package pro.kensait.leafbooks.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import pro.kensait.leafbooks.entity.Publisher;

@ApplicationScoped
public class PublisherDao {
    private static final Logger logger = LoggerFactory.getLogger(PublisherDao.class);

    @PersistenceContext(unitName = "bookstorePU")
    private EntityManager em;

    public Publisher findById(Integer publisherId) {
        logger.info("[ PublisherDao#findById ]");
        return em.find(Publisher.class, publisherId);
    }

    public List<Publisher> findAll() {
        logger.info("[ PublisherDao#findAll ]");
        TypedQuery<Publisher> query = em.createQuery(
                "SELECT p FROM Publisher p ORDER BY p.publisherId", Publisher.class);
        return query.getResultList();
    }
}
