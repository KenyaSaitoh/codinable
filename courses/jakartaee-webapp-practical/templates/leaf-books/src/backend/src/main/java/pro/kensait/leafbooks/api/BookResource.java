package pro.kensait.leafbooks.api;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pro.kensait.leafbooks.api.dto.BookTO;
import pro.kensait.leafbooks.entity.Book;
import pro.kensait.leafbooks.service.book.BookService;
import pro.kensait.leafbooks.service.category.CategoryService;

/**
 * 書籍APIリソースクラス
 * 直接DBアクセスで書籍情報を提供する
 */
@Path("/books")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class BookResource {
    private static final Logger logger = LoggerFactory.getLogger(BookResource.class);

    @Inject
    private BookService bookService;

    @Inject
    private CategoryService categoryService;

    /**
     * 書籍一覧取得
     * GET /api/books
     */
    @GET
    public Response getAllBooks() {
        logger.info("[ BookResource#getAllBooks ]");
        List<BookTO> books = bookService.getBooksAll().stream()
                .map(this::toBookTO)
                .collect(Collectors.toList());
        return Response.ok(books).build();
    }

    /**
     * 書籍詳細取得
     * GET /api/books/{id}
     */
    @GET
    @Path("/{id}")
    public Response getBookById(@PathParam("id") Integer id) {
        logger.info("[ BookResource#getBookById ] id: {}", id);
        Book book = bookService.getBook(id);
        if (book == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"書籍が見つかりません\"}")
                    .build();
        }
        return Response.ok(toBookTO(book)).build();
    }

    /**
     * 書籍検索（静的クエリ - JPQL）
     * GET /api/books/search/jpql?categoryId=&keyword=
     */
    @GET
    @Path("/search/jpql")
    public Response searchBooksJpql(
            @QueryParam("categoryId") Integer categoryId,
            @QueryParam("keyword") String keyword) {
        logger.info("[ BookResource#searchBooksJpql ] categoryId: {}, keyword: {}", categoryId, keyword);

        List<Book> books;
        if (categoryId != null && categoryId != 0) {
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

        List<BookTO> bookTOs = books.stream().map(this::toBookTO).collect(Collectors.toList());
        return Response.ok(bookTOs).build();
    }

    /**
     * 書籍検索（動的クエリ - Criteria API）
     * GET /api/books/search/criteria?categoryId=&keyword=
     */
    @GET
    @Path("/search/criteria")
    public Response searchBooksCriteria(
            @QueryParam("categoryId") Integer categoryId,
            @QueryParam("keyword") String keyword) {
        logger.info("[ BookResource#searchBooksCriteria ] categoryId: {}, keyword: {}", categoryId, keyword);
        List<BookTO> books = bookService.searchBookWithCriteria(categoryId, keyword)
                .stream().map(this::toBookTO).collect(Collectors.toList());
        return Response.ok(books).build();
    }

    /**
     * 書籍検索（後方互換 - デフォルトはJPQL）
     * GET /api/books/search?categoryId=&keyword=
     */
    @GET
    @Path("/search")
    public Response searchBooks(
            @QueryParam("categoryId") Integer categoryId,
            @QueryParam("keyword") String keyword) {
        logger.info("[ BookResource#searchBooks ] categoryId: {}, keyword: {}", categoryId, keyword);
        return searchBooksJpql(categoryId, keyword);
    }

    /**
     * カテゴリ一覧取得（BookResourceから提供 - 後方互換）
     * GET /api/books/categories
     */
    @GET
    @Path("/categories")
    public Response getAllCategories() {
        logger.info("[ BookResource#getAllCategories ]");
        return Response.ok(categoryService.getCategoryMap()).build();
    }

    private BookTO toBookTO(Book book) {
        BookTO.CategoryInfo category = null;
        if (book.getCategory() != null) {
            category = new BookTO.CategoryInfo(
                    book.getCategory().getCategoryId(),
                    book.getCategory().getCategoryName());
        }
        BookTO.PublisherInfo publisher = null;
        if (book.getPublisher() != null) {
            publisher = new BookTO.PublisherInfo(
                    book.getPublisher().getPublisherId(),
                    book.getPublisher().getPublisherName());
        }
        return new BookTO(
                book.getBookId(),
                book.getBookName(),
                book.getAuthor(),
                book.getPrice(),
                book.getImageUrl(),
                book.getQuantity(),
                book.getVersion(),
                category,
                publisher);
    }
}
