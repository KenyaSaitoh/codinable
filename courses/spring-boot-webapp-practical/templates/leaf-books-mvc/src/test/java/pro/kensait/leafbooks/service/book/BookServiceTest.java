package pro.kensait.leafbooks.service.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import pro.kensait.leafbooks.entity.Book;
import pro.kensait.leafbooks.entity.Category;
import pro.kensait.leafbooks.entity.Publisher;
import pro.kensait.leafbooks.repository.BookRepository;

/*
 * 書籍のテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookServiceのテスト")
class BookServiceTest {
    
    // テスト対象クラス
    @InjectMocks
    private BookService bookService;
    
    // テスト対象クラスの呼び出し先
    @Mock
    private BookRepository bookRepository;
    
    @Mock
    private EntityManager entityManager;
    
    // すべてのテストケースで共通的なフィクスチャ
    private Book testBook;
    
    // 各テストケースで共通的な前処理
    @BeforeEach
    void setUp() {
        Category category = new Category(1, "Java");
        Publisher publisher = new Publisher(3, "ネットワークノード出版");
        testBook = new Book(1, "Java SEディープダイブ", "Michael Johnson", 
                category, publisher, BigDecimal.valueOf(3400), 3);
    }
    
    /*
     * get書籍のテスト
     */
    @Nested
    @DisplayName("書籍取得のテスト")
    class GetBookTest {
        
        // 「IDで書籍を取得できること」の検証
        @Test
        @DisplayName("IDで書籍を取得できること")
        void test_getBookById() {
            // 準備フェーズ：BookRepositoryのモック動作を設定する
            when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
            
            // 実行フェーズ：IDで書籍を取得する
            Book result = bookService.getBook(1);
            
            // 検証フェーズ：期待値と実測値が一致しているかを検証する
            assertNotNull(result);
            assertEquals("Java SEディープダイブ", result.getBookName());
            verify(bookRepository, times(1)).findById(1);
        }
        
        // 「存在しないIDで例外がスローされること」の検証
        @Test
        @DisplayName("存在しないIDで例外がスローされること")
        void test_getBookThrowsExceptionForNonExistentId() {
            // 準備フェーズ：BookRepositoryのモック動作を設定する（存在しないID）
            when(bookRepository.findById(999)).thenReturn(Optional.empty());
            
            // 実行・検証フェーズ：書籍取得を試み、例外がスローされることを検証する
            assertThrows(RuntimeException.class, () -> bookService.getBook(999));
        }
        
        // 「全書籍を取得できること」の検証
        @Test
        @DisplayName("全書籍を取得できること")
        void test_getAllBooks() {
            // 準備フェーズ：BookRepositoryのモック動作を設定する
            List<Book> books = Arrays.asList(testBook);
            when(bookRepository.findAll()).thenReturn(books);
            
            // 実行フェーズ：全書籍を取得する
            List<Book> result = bookService.getBooksAll();
            
            // 検証フェーズ：期待値と実測値が一致しているかを検証する
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(bookRepository, times(1)).findAll();
        }
    }
    
    /*
     * staticクエリのテスト
     */
    @Nested
    @DisplayName("静的クエリでの書籍検索のテスト")
    class StaticQueryTest {
        
        // 「カテゴリIDとキーワードで書籍を検索できること」の検証
        @Test
        @DisplayName("カテゴリIDとキーワードで書籍を検索できること")
        void test_searchBookByCategoryIdAndKeyword() {
            // 準備フェーズ：BookRepositoryのモック動作を設定する
            Integer categoryId = 1;
            String keyword = "Java";
            List<Book> books = Arrays.asList(testBook);
            when(bookRepository.query(categoryId, "%Java%")).thenReturn(books);
            
            // 実行フェーズ：カテゴリIDとキーワードで書籍を検索する
            List<Book> result = bookService.searchBook(categoryId, keyword);
            
            // 検証フェーズ：期待値と実測値が一致しているかを検証する
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(bookRepository, times(1)).query(categoryId, "%Java%");
        }
        
        // 「カテゴリIDで書籍を検索できること」の検証
        @Test
        @DisplayName("カテゴリIDで書籍を検索できること")
        void test_searchBookByCategoryId() {
            // 準備フェーズ：BookRepositoryのモック動作を設定する
            Integer categoryId = 1;
            List<Book> books = Arrays.asList(testBook);
            when(bookRepository.query(categoryId)).thenReturn(books);
            
            // 実行フェーズ：カテゴリIDで書籍を検索する
            List<Book> result = bookService.searchBook(categoryId);
            
            // 検証フェーズ：期待値と実測値が一致しているかを検証する
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(bookRepository, times(1)).query(categoryId);
        }
        
        // 「キーワードで書籍を検索できること」の検証
        @Test
        @DisplayName("キーワードで書籍を検索できること")
        void test_searchBookByKeyword() {
            // 準備フェーズ：BookRepositoryのモック動作を設定する
            String keyword = "Java";
            List<Book> books = Arrays.asList(testBook);
            when(bookRepository.query("%Java%")).thenReturn(books);
            
            // 実行フェーズ：キーワードで書籍を検索する
            List<Book> result = bookService.searchBook(keyword);
            
            // 検証フェーズ：期待値と実測値が一致しているかを検証する
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(bookRepository, times(1)).query("%Java%");
        }
    }
    
    /*
     * 検索条件クエリのテスト
     */
    @Nested
    @DisplayName("動的クエリ（Criteria API）での書籍検索のテスト")
    class CriteriaQueryTest {
        
        // 「Criteriaで動的クエリを使って検索できること」の検証
        @Test
        @DisplayName("Criteriaで動的クエリを使って検索できること")
        void test_searchBookWithCriteria() {
            // 準備フェーズ：CriteriaBuilderとその他のモックオブジェクトを生成する
            Integer categoryId = 1;
            String keyword = "Java";
            
            CriteriaBuilder cb = org.mockito.Mockito.mock(CriteriaBuilder.class);
            @SuppressWarnings("unchecked")
            CriteriaQuery<Book> cq = org.mockito.Mockito.mock(CriteriaQuery.class);
            @SuppressWarnings("unchecked")
            Root<Book> root = org.mockito.Mockito.mock(Root.class);
            @SuppressWarnings("unchecked")
            TypedQuery<Book> typedQuery = org.mockito.Mockito.mock(TypedQuery.class);
            @SuppressWarnings("unchecked")
            Path<Object> categoryPath = org.mockito.Mockito.mock(Path.class);
            @SuppressWarnings("unchecked")
            Path<Object> categoryIdPath = org.mockito.Mockito.mock(Path.class);
            @SuppressWarnings("unchecked")
            Path<Object> bookNamePath = org.mockito.Mockito.mock(Path.class);
            Predicate predicate = org.mockito.Mockito.mock(Predicate.class);
            
            lenient().when(entityManager.getCriteriaBuilder()).thenReturn(cb);
            lenient().when(cb.createQuery(Book.class)).thenReturn(cq);
            lenient().when(cq.from(Book.class)).thenReturn(root);
            lenient().when(cb.conjunction()).thenReturn(predicate);
            lenient().when(root.get("category")).thenReturn(categoryPath);
            lenient().when(categoryPath.get("categoryId")).thenReturn(categoryIdPath);
            lenient().when(root.get("bookName")).thenReturn(bookNamePath);
            lenient().when(cb.equal(any(), any())).thenReturn(predicate);
            lenient().when(cb.like(any(), anyString())).thenReturn(predicate);
            lenient().when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
            lenient().when(cq.select(root)).thenReturn(cq);
            lenient().when(cq.where(predicate)).thenReturn(cq);
            lenient().when(entityManager.createQuery(cq)).thenReturn(typedQuery);
            lenient().when(typedQuery.getResultList()).thenReturn(Arrays.asList(testBook));
            
            // 実行フェーズ：Criteriaで動的クエリを使って書籍を検索する
            List<Book> result = bookService.searchBookWithCriteria(categoryId, keyword);
            
            // 検証フェーズ：期待値と実測値が一致しているかを検証する
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(entityManager, times(1)).getCriteriaBuilder();
        }
    }
}
