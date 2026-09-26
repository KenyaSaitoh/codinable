package pro.kensait.leafbooks.web.book;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import pro.kensait.leafbooks.entity.Book;
import pro.kensait.leafbooks.entity.Category;
import pro.kensait.leafbooks.entity.Publisher;
import pro.kensait.leafbooks.service.book.BookService;
import pro.kensait.leafbooks.service.category.CategoryService;

/*
 * 書籍のテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookControllerのテスト")
class BookControllerTest {
    
    private MockMvc mockMvc;
    private BookController bookController;
    
    // テスト対象クラスの呼び出し先（モック）
    @Mock
    private BookService bookService;
    
    @Mock
    private CategoryService categoryService;
    
    // すべてのテストケースで共通的なフィクスチャ
    private Book testBook;
    private List<Book> bookList;
    
    // 各テストケースで共通的な前処理
    @BeforeEach
    void setUp() throws Exception {
        // BookControllerインスタンスを作成し、モックを手動で注入する
        bookController = new BookController();
        java.lang.reflect.Field bookServiceField = BookController.class.getDeclaredField("bookService");
        bookServiceField.setAccessible(true);
        bookServiceField.set(bookController, bookService);
        
        java.lang.reflect.Field categoryServiceField = BookController.class.getDeclaredField("categoryService");
        categoryServiceField.setAccessible(true);
        categoryServiceField.set(bookController, categoryService);
        
        // MockMvcをスタンドアロンモードでセットアップする
        mockMvc = MockMvcBuilders.standaloneSetup(bookController).build();
        
        Category category = new Category(1, "Java");
        Publisher publisher = new Publisher(3, "ネットワークノード出版");
        testBook = new Book(1, "Java SEディープダイブ", "Michael Johnson", 
                category, publisher, BigDecimal.valueOf(3400), 3);
        bookList = Arrays.asList(testBook);
    }
    
    /*
     * ナビゲーションのテスト
     */
    @Nested
    @DisplayName("画面遷移のテスト")
    class NavigationTest {
        
        // 「書籍選択ページに遷移できること」の検証
        @Test
        @DisplayName("書籍選択ページに遷移できること")
        void test_toSelect() throws Exception {
            // 準備フェーズ：BookServiceのモック動作を設定する
            when(bookService.getBooksAll()).thenReturn(bookList);
            
            // 実行・検証フェーズ
            mockMvc.perform(get("/toSelect"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("BookSelectPage"))
                    .andExpect(model().attributeExists("bookList"));
            
            verify(bookService, times(1)).getBooksAll();
        }
        
        // 「書籍検索ページに遷移できること」の検証
        @Test
        @DisplayName("書籍検索ページに遷移できること")
        void test_toSearch() throws Exception {
            // 準備フェーズ：CategoryServiceのモック動作を設定する
            Map<String, Integer> categoryMap = new HashMap<>();
            categoryMap.put("Java", 1);
            categoryMap.put("SpringBoot", 2);
            when(categoryService.getCategoryMap()).thenReturn(categoryMap);
            
            // 実行・検証フェーズ
            mockMvc.perform(get("/toSearch"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("BookSearchPage"))
                    .andExpect(model().attributeExists("categoryMap"));
            
            verify(categoryService, times(1)).getCategoryMap();
        }
    }
    
    /*
     * 検索のテスト
     */
    @Nested
    @DisplayName("書籍検索のテスト")
    class SearchTest {
        
        // 「カテゴリIDとキーワードで書籍を検索できること」の検証
        @Test
        @DisplayName("カテゴリIDとキーワードで書籍を検索できること")
        void test_searchWithCategoryIdAndKeyword() throws Exception {
            // 準備フェーズ：BookServiceのモック動作を設定する
            when(bookService.searchBook(eq(1), eq("Java"))).thenReturn(bookList);
            
            // 実行・検証フェーズ
            mockMvc.perform(get("/search")
                            .param("categoryId", "1")
                            .param("keyword", "Java"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("BookSelectPage"))
                    .andExpect(model().attributeExists("bookList"));
            
            verify(bookService, times(1)).searchBook(1, "Java");
        }
        
        // 「カテゴリIDのみで書籍を検索できること」の検証
        @Test
        @DisplayName("カテゴリIDのみで書籍を検索できること")
        void test_searchWithCategoryIdOnly() throws Exception {
            // 準備フェーズ：BookServiceのモック動作を設定する
            when(bookService.searchBook(eq(1))).thenReturn(bookList);
            
            // 実行・検証フェーズ
            mockMvc.perform(get("/search")
                            .param("categoryId", "1")
                            .param("keyword", ""))
                    .andExpect(status().isOk())
                    .andExpect(view().name("BookSelectPage"))
                    .andExpect(model().attributeExists("bookList"));
            
            verify(bookService, times(1)).searchBook(1);
        }
        
        // 「キーワードのみで書籍を検索できること」の検証
        @Test
        @DisplayName("キーワードのみで書籍を検索できること")
        void test_searchWithKeywordOnly() throws Exception {
            // 準備フェーズ：BookServiceのモック動作を設定する
            when(bookService.searchBook(eq("Java"))).thenReturn(bookList);
            
            // 実行・検証フェーズ
            mockMvc.perform(get("/search")
                            .param("keyword", "Java"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("BookSelectPage"))
                    .andExpect(model().attributeExists("bookList"));
            
            verify(bookService, times(1)).searchBook("Java");
        }
        
        // 「動的クエリで書籍を検索できること」の検証
        @Test
        @DisplayName("動的クエリで書籍を検索できること")
        void test_search2WithCriteria() throws Exception {
            // 準備フェーズ：BookServiceのモック動作を設定する
            when(bookService.searchBookWithCriteria(any(), any())).thenReturn(bookList);
            
            // 実行・検証フェーズ
            mockMvc.perform(get("/search2")
                            .param("categoryId", "1")
                            .param("keyword", "Java"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("BookSelectPage"))
                    .andExpect(model().attributeExists("bookList"));
            
            verify(bookService, times(1)).searchBookWithCriteria(1, "Java");
        }
    }
}
