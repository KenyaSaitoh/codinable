package pro.kensait.leafbooks.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import pro.kensait.leafbooks.entity.Category;

@ApplicationScoped
public class CategoryDao {
    private static final Logger logger = LoggerFactory.getLogger(CategoryDao.class);

    @PersistenceContext(unitName = "bookstorePU")
    private EntityManager em;

    public Category findById(Integer categoryId) {
        logger.info("[ CategoryDao#findById ]");
        return em.find(Category.class, categoryId);
    }

    public List<Category> findAll() {
        logger.info("[ CategoryDao#findAll ]");
        TypedQuery<Category> query = em.createQuery(
                "SELECT c FROM Category c", Category.class);
        return query.getResultList();
    }
}
