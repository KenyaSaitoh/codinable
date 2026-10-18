package pro.kensait.leafbooks.dao;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import pro.kensait.leafbooks.entity.Book;

@ApplicationScoped
public class BookDao {
    private static final Logger logger = LoggerFactory.getLogger(BookDao.class);

    @PersistenceContext(unitName = "bookstorePU")
    private EntityManager em;

    public Book findById(Integer bookId) {
        logger.info("[ BookDao#findById ]");
        return em.find(Book.class, bookId);
    }

    public List<Book> findAll() {
        logger.info("[ BookDao#findAll ]");
        TypedQuery<Book> query = em.createQuery(
                "SELECT b FROM Book b WHERE (b.deleted = false OR b.deleted IS NULL)", Book.class);
        List<Book> books = query.getResultList();
        for (Book book : books) {
            em.refresh(book);
        }
        return books;
    }

    public List<Book> queryByCategory(Integer categoryId) {
        logger.info("[ BookDao#queryByCategory ]");
        TypedQuery<Book> query = em.createQuery(
                "SELECT b FROM Book b WHERE b.category.categoryId = :categoryId " +
                "AND (b.deleted = false OR b.deleted IS NULL)", Book.class);
        query.setParameter("categoryId", categoryId);
        List<Book> books = query.getResultList();
        for (Book book : books) {
            em.refresh(book);
        }
        return books;
    }

    public List<Book> queryByKeyword(String keyword) {
        logger.info("[ BookDao#queryByKeyword ]");
        TypedQuery<Book> query = em.createQuery(
                "SELECT b FROM Book b WHERE b.bookName like :keyword " +
                "AND (b.deleted = false OR b.deleted IS NULL)", Book.class);
        query.setParameter("keyword", keyword);
        List<Book> books = query.getResultList();
        for (Book book : books) {
            em.refresh(book);
        }
        return books;
    }

    public List<Book> query(Integer categoryId, String keyword) {
        logger.info("[ BookDao#query ]");
        TypedQuery<Book> query = em.createQuery(
                "SELECT b FROM Book b WHERE b.category.categoryId = :categoryId " +
                "AND b.bookName like :keyword " +
                "AND (b.deleted = false OR b.deleted IS NULL)", Book.class);
        query.setParameter("categoryId", categoryId);
        query.setParameter("keyword", keyword);
        List<Book> books = query.getResultList();
        for (Book book : books) {
            em.refresh(book);
        }
        return books;
    }

    public List<Book> searchWithCriteria(Integer categoryId, String keyword) {
        logger.info("[ BookDao#searchWithCriteria ]");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Book> cq = cb.createQuery(Book.class);
        Root<Book> book = cq.from(Book.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.or(
            cb.equal(book.get("deleted"), false),
            cb.isNull(book.get("deleted"))
        ));
        if (categoryId != null) {
            predicates.add(cb.equal(book.get("category").get("categoryId"), categoryId));
        }
        if (keyword != null && !keyword.isEmpty()) {
            predicates.add(cb.like(book.get("bookName"), keyword));
        }
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        TypedQuery<Book> query = em.createQuery(cq);
        List<Book> books = query.getResultList();
        for (Book b : books) {
            em.refresh(b);
        }
        return books;
    }
}
