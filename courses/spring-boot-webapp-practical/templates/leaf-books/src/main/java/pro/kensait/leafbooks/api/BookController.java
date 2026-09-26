package pro.kensait.leafbooks.api;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pro.kensait.leafbooks.entity.Book;
import pro.kensait.leafbooks.service.book.BookService;
import pro.kensait.leafbooks.service.category.CategoryService;

/*
 * 書籍機能のコントローラー
 */
@RestController
@RequestMapping("/api")
public class BookController {
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    @Autowired
    private BookService bookService;

    @Autowired
    private CategoryService categoryService;

    // 書籍一覧取得
    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks() {
        logger.info("[ BookController#getAllBooks ]");

        List<Book> books = bookService.getBooksAll();
        return ResponseEntity.ok(books);
    }

    // 書籍詳細取得
    @GetMapping("/books/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Integer id) {
        logger.info("[ BookController#getBookById ] id: {}", id);

        Book book = bookService.getBook(id);
        return ResponseEntity.ok(book);
    }

    // 書籍検索（静的クエリ - JPQL）
    @GetMapping("/books/search/jpql")
    public ResponseEntity<List<Book>> searchBooksJpql(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword) {
        logger.info("[ BookController#searchBooksJpql ] categoryId: {}, keyword: {}", categoryId, keyword);

        List<Book> books;

        if (categoryId != null) {
            if (keyword != null && !keyword.isEmpty()) {
                books = bookService.searchBook(categoryId, keyword);
            } else {
                books = bookService.searchBook(categoryId);
            }
        } else {
            if (keyword != null && !keyword.isEmpty()) {
                books = bookService.searchBook(keyword);
            } else {
                books = bookService.getBooksAll();
            }
        }

        return ResponseEntity.ok(books);
    }

    // 書籍検索（動的クエリ - Criteria API）
    @GetMapping("/books/search/criteria")
    public ResponseEntity<List<Book>> searchBooksCriteria(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword) {
        logger.info("[ BookController#searchBooksCriteria ] categoryId: {}, keyword: {}", categoryId, keyword);

        List<Book> books = bookService.searchBookWithCriteria(categoryId, keyword);
        return ResponseEntity.ok(books);
    }

    // 書籍検索（後方互換性のため残す - デフォルトはJPQL）
    @GetMapping("/books/search")
    public ResponseEntity<List<Book>> searchBooks(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword) {
        logger.info("[ BookController#searchBooks ] categoryId: {}, keyword: {}", categoryId, keyword);
        // デフォルトはJPQLを使用
        return searchBooksJpql(categoryId, keyword);
    }

    // カテゴリ一覧取得
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Integer>> getAllCategories() {
        logger.info("[ BookController#getAllCategories ]");

        Map<String, Integer> categories = categoryService.getCategoryMap();
        return ResponseEntity.ok(categories);
    }
}

