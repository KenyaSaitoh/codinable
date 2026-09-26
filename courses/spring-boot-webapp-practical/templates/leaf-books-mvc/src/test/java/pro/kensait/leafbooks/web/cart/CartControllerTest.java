package pro.kensait.leafbooks.web.cart;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/*
 * カートのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartControllerのテスト")
class CartControllerTest {
    
    private MockMvc mockMvc;
    private CartController cartController;
    
    // テスト対象クラスの呼び出し先（モック）
    @Mock
    private CartSession cartSession;
    
    @Mock
    private pro.kensait.leafbooks.service.book.BookService bookService;
    
    @Mock
    private pro.kensait.leafbooks.repository.StockRepository stockRepository;
    
    @Mock
    private org.springframework.context.MessageSource messageSource;
    
    // すべてのテストケースで共通的なフィクスチャ
    private CartItem testCartItem;
    
    // 各テストケースで共通的な前処理
    @BeforeEach
    void setUp() throws Exception {
        // CartControllerインスタンスを作成し、モックを手動で注入する
        cartController = new CartController();
        java.lang.reflect.Field bookServiceField = CartController.class.getDeclaredField("bookService");
        bookServiceField.setAccessible(true);
        bookServiceField.set(cartController, bookService);
        
        java.lang.reflect.Field stockRepositoryField = CartController.class.getDeclaredField("stockRepository");
        stockRepositoryField.setAccessible(true);
        stockRepositoryField.set(cartController, stockRepository);
        
        java.lang.reflect.Field messageSourceField = CartController.class.getDeclaredField("messageSource");
        messageSourceField.setAccessible(true);
        messageSourceField.set(cartController, messageSource);
        
        // MockMvcをスタンドアロンモードでセットアップする
        mockMvc = MockMvcBuilders.standaloneSetup(cartController).build();
        
        testCartItem = new CartItem(1, "Java SEディープダイブ", "ネットワークノード出版", 
                BigDecimal.valueOf(3400), 1, false, 0L);
    }
    
    /*
     * カートoperationのテスト
     */
    @Nested
    @DisplayName("カート操作のテスト")
    class CartOperationTest {
        
        // Note: /addBook テストはBookServiceとStockRepositoryの複雑なモック設定が必要なため、
        // 統合テストで検証することを推奨
        
        @Test
        @DisplayName("カートから商品を削除できること")
        void test_removeFromCart() throws Exception {
            // 実行・検証フェーズ
            mockMvc.perform(post("/removeBook")
                            .sessionAttr("cartSession", cartSession)
                            .param("removeBookIdList", "1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("CartViewPage"));
        }
        
        // Note: /changeQuantity エンドポイントは実装されていません
        // 数量変更は /addBook を複数回呼び出すことで実現
        
        @Test
        @DisplayName("カートをクリアできること")
        void test_clearCart() throws Exception {
            // 実行・検証フェーズ
            mockMvc.perform(post("/clear")
                            .sessionAttr("cartSession", cartSession))
                    .andExpect(status().isOk())
                    .andExpect(view().name("CartClearPage"));
        }
    }
}
