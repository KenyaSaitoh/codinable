package pro.kensait.leafbooks.service.book;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import pro.kensait.leafbooks.common.MessageUtil;
import pro.kensait.leafbooks.dao.BookDao;
import pro.kensait.leafbooks.entity.Book;

@ApplicationScoped
@Transactional
public class BookService {
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    @Inject
    private BookDao bookDao;

    public Book getBook(Integer bookId) {
        logger.info("[ BookService#getBook ]");
        Book book = bookDao.findById(bookId);
        if (book == null) {
            throw new NotFoundException(MessageUtil.get("error.book.not-found") + bookId);
        }
        return book;
    }

    public List<Book> getBooksAll() {
        logger.info("[ BookService#getBooksAll ]");
        return bookDao.findAll();
    }

    public List<Book> searchBook(Integer categoryId, String keyword) {
        logger.info("[ BookService#searchBook(categoryId, keyword) ]");
        return bookDao.query(categoryId, toLikeWord(keyword));
    }

    public List<Book> searchBook(Integer categoryId) {
        logger.info("[ BookService#searchBook(categoryId) ]");
        return bookDao.queryByCategory(categoryId);
    }

    public List<Book> searchBook(String keyword) {
        logger.info("[ BookService#searchBook(keyword) ]");
        return bookDao.queryByKeyword(toLikeWord(keyword));
    }

    public List<Book> searchBookWithCriteria(Integer categoryId, String keyword) {
        logger.info("[ BookService#searchBookWithCriteria ]");
        String likeKeyword = (keyword != null && !keyword.isEmpty()) ? toLikeWord(keyword) : null;
        return bookDao.searchWithCriteria(categoryId, likeKeyword);
    }

    private String toLikeWord(String keyword) {
        return "%" + keyword + "%";
    }
}
