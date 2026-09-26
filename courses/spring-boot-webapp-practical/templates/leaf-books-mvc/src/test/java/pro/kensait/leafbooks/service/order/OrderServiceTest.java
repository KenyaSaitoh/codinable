package pro.kensait.leafbooks.service.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
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

import jakarta.persistence.OptimisticLockException;
import pro.kensait.leafbooks.entity.Book;
import pro.kensait.leafbooks.entity.Category;
import pro.kensait.leafbooks.entity.OrderDetail;
import pro.kensait.leafbooks.entity.OrderDetailPK;
import pro.kensait.leafbooks.entity.OrderTran;
import pro.kensait.leafbooks.entity.Publisher;
import pro.kensait.leafbooks.entity.Stock;
import pro.kensait.leafbooks.repository.BookRepository;
import pro.kensait.leafbooks.repository.OrderDetailRepository;
import pro.kensait.leafbooks.repository.OrderTranRepository;
import pro.kensait.leafbooks.repository.StockRepository;
import pro.kensait.leafbooks.web.cart.CartItem;

/*
 * CartItemは同じパッケージ内のため、import不要
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceのテスト")
class OrderServiceTest {
    
    // テスト対象クラス
    @InjectMocks
    private OrderService orderService;
    
    // テスト対象クラスの呼び出し先
    @Mock
    private OrderTranRepository orderTranRepository;
    
    @Mock
    private OrderDetailRepository orderDetailRepository;
    
    @Mock
    private BookRepository bookRepository;
    
    @Mock
    private StockRepository stockRepository;
    
    // すべてのテストケースで共通的なフィクスチャ
    private OrderTran testOrderTran;
    private Book testBook;
    private Stock testStock;
    
    // 各テストケースで共通的な前処理
    @BeforeEach
    void setUp() {
        Category category = new Category(1, "Java");
        Publisher publisher = new Publisher(3, "ネットワークノード出版");
        testBook = new Book(1, "Java SEディープダイブ", "Michael Johnson", 
                category, publisher, BigDecimal.valueOf(3400), 3);
        testStock = new Stock(1, 3, 0L);
        
        testOrderTran = new OrderTran(LocalDate.of(2023, 3, 1), 1, 
                BigDecimal.valueOf(3400), BigDecimal.valueOf(500), 
                "東京都中央区1-1-1", 1);
        testOrderTran.setOrderTranId(1);
    }
    
    /*
     * get注文履歴のテスト
     */
    @Nested
    @DisplayName("注文履歴取得のテスト")
    class GetOrderHistoryTest {
        
        // 「顧客IDで注文履歴を取得できること」の検証
        @Test
        @DisplayName("顧客IDで注文履歴を取得できること")
        void test_getOrderHistory() {
            // 準備フェーズ：OrderTranRepositoryのモック動作を設定する
            List<OrderTran> orderTrans = Arrays.asList(testOrderTran);
            when(orderTranRepository.findByCustomer(1)).thenReturn(orderTrans);
            
            // 実行フェーズ：顧客IDで注文履歴を取得する
            List<OrderTran> result = orderService.getOrderHistory(1);
            
            // 検証フェーズ：期待値と実測値が一致しているかを検証する
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(orderTranRepository, times(1)).findByCustomer(1);
        }
        
        // 「顧客IDで注文履歴TOを取得できること」の検証
        @Test
        @DisplayName("顧客IDで注文履歴TOを取得できること")
        void test_getOrderHistory2() {
            // 準備フェーズ：OrderTranRepositoryのモック動作を設定する
            OrderHistoryTO orderHistoryTO = new OrderHistoryTO(
                    LocalDate.of(2023, 3, 1), 1, 1, 
                    "Java SEディープダイブ", "ネットワークノード出版", 
                    BigDecimal.valueOf(3400), 1);
            List<OrderHistoryTO> orderHistories = Arrays.asList(orderHistoryTO);
            when(orderTranRepository.findOrderHistoryTOByCustomer(1))
                    .thenReturn(orderHistories);
            
            // 実行フェーズ：顧客IDで注文履歴TOを取得する
            List<OrderHistoryTO> result = orderService.getOrderHistory2(1);
            
            // 検証フェーズ：期待値と実測値が一致しているかを検証する
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Java SEディープダイブ", result.get(0).bookName());
            verify(orderTranRepository, times(1)).findOrderHistoryTOByCustomer(1);
        }
    }
    
    /*
     * get注文のテスト
     */
    @Nested
    @DisplayName("注文・注文明細取得のテスト")
    class GetOrderTest {
        
        // 「注文IDで注文を取得できること」の検証
        @Test
        @DisplayName("注文IDで注文を取得できること")
        void test_getOrderTran() {
            // 準備フェーズ：OrderTranRepositoryのモック動作を設定する
            when(orderTranRepository.findByIdWithDetails(1)).thenReturn(testOrderTran);
            
            // 実行フェーズ：注文IDで注文を取得する
            OrderTran result = orderService.getOrderTran(1);
            
            // 検証フェーズ：期待値と実測値が一致しているかを検証する
            assertNotNull(result);
            assertEquals(1, result.getCustomer());
            verify(orderTranRepository, times(1)).findByIdWithDetails(1);
        }
        
        // 「存在しない注文IDで例外がスローされること」の検証
        @Test
        @DisplayName("存在しない注文IDで例外がスローされること")
        void test_getOrderTranThrowsExceptionForNonExistentId() {
            // 準備フェーズ：OrderTranRepositoryのモック動作を設定する（存在しないID）
            when(orderTranRepository.findByIdWithDetails(999)).thenReturn(null);
            
            // 実行・検証フェーズ：注文取得を試み、例外がスローされることを検証する
            assertThrows(RuntimeException.class, () -> orderService.getOrderTran(999));
        }
        
        // 「複合主キーで注文明細を取得できること」の検証
        @Test
        @DisplayName("複合主キーで注文明細を取得できること")
        void test_getOrderDetail() {
            // 準備フェーズ：OrderDetailRepositoryのモック動作を設定する
            OrderDetailPK pk = new OrderDetailPK(1, 1);
            OrderDetail orderDetail = new OrderDetail(1, 1, testBook, 1);
            when(orderDetailRepository.findById(pk)).thenReturn(Optional.of(orderDetail));
            
            // 実行フェーズ：複合主キーで注文明細を取得する
            OrderDetail result = orderService.getOrderDetail(pk);
            
            // 検証フェーズ：期待値と実測値が一致しているかを検証する
            assertNotNull(result);
            assertEquals(1, result.getCount());
            verify(orderDetailRepository, times(1)).findById(pk);
        }
    }
    
    /*
     * 注文書籍のテスト
     */
    @Nested
    @DisplayName("注文処理のテスト")
    class OrderBooksTest {
        
        // 「書籍を注文できること」の検証
        @Test
        @DisplayName("書籍を注文できること")
        void test_orderBooks() {
            // 準備フェーズ：テストフィクスチャとRepositoryのモック動作を設定する
            CartItem cartItem = new CartItem(1, "Java SEディープダイブ", 
                    "ネットワークノード出版", BigDecimal.valueOf(3400), 1, false, 0L);
            List<CartItem> cartItems = Arrays.asList(cartItem);
            OrderTO orderTO = new OrderTO(1, LocalDate.now(), cartItems, 
                    BigDecimal.valueOf(3400), BigDecimal.valueOf(500), 
                    "東京都中央区1-1-1", 1);
            
            when(stockRepository.findById(1)).thenReturn(Optional.of(testStock));
            when(stockRepository.updateQuantity(1, 1, 0L)).thenReturn(1);
            when(orderTranRepository.save(any(OrderTran.class))).thenReturn(testOrderTran);
            when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
            when(orderDetailRepository.save(any(OrderDetail.class)))
                    .thenReturn(new OrderDetail());
            
            // 実行フェーズ：書籍を注文する
            OrderTran result = orderService.orderBooks(orderTO);
            
            // 検証フェーズ：期待値と実測値が一致しているかを検証する
            assertNotNull(result);
            verify(stockRepository, times(1)).updateQuantity(1, 1, 0L);
            verify(orderTranRepository, times(1)).save(any(OrderTran.class));
            verify(orderDetailRepository, times(1)).save(any(OrderDetail.class));
        }
        
        // 「在庫不足の場合は例外がスローされること」の検証
        @Test
        @DisplayName("在庫不足の場合は例外がスローされること")
        void test_orderBooksThrowsOutOfStockException() {
            // 準備フェーズ：在庫不足のテストフィクスチャとStockRepositoryのモック動作を設定する
            Stock lowStock = new Stock(1, 0, 0L);
            CartItem cartItem = new CartItem(1, "Java SEディープダイブ", 
                    "ネットワークノード出版", BigDecimal.valueOf(3400), 1, false, 0L);
            List<CartItem> cartItems = Arrays.asList(cartItem);
            OrderTO orderTO = new OrderTO(1, LocalDate.now(), cartItems, 
                    BigDecimal.valueOf(3400), BigDecimal.valueOf(500), 
                    "東京都中央区1-1-1", 1);
            
            when(stockRepository.findById(1)).thenReturn(Optional.of(lowStock));
            
            // 実行・検証フェーズ：注文を試み、在庫不足例外がスローされることを検証する
            assertThrows(OutOfStockException.class, () -> orderService.orderBooks(orderTO));
        }
        
        // 「楽観的ロックでバージョン不一致の場合は例外がスローされること」の検証
        @Test
        @DisplayName("楽観的ロックでバージョン不一致の場合は例外がスローされること")
        void test_orderBooksThrowsOptimisticLockException() {
            // 準備フェーズ：テストフィクスチャとRepositoryのモック動作を設定する（更新失敗をシミュレート）
            CartItem cartItem = new CartItem(1, "Java SEディープダイブ", 
                    "ネットワークノード出版", BigDecimal.valueOf(3400), 1, false, 0L);
            List<CartItem> cartItems = Arrays.asList(cartItem);
            OrderTO orderTO = new OrderTO(1, LocalDate.now(), cartItems, 
                    BigDecimal.valueOf(3400), BigDecimal.valueOf(500), 
                    "東京都中央区1-1-1", 1);
            
            when(stockRepository.findById(1)).thenReturn(Optional.of(testStock));
            when(stockRepository.updateQuantity(1, 1, 0L)).thenReturn(0);
            
            // 実行・検証フェーズ：注文を試み、楽観的ロック例外がスローされることを検証する
            assertThrows(OptimisticLockException.class, () -> orderService.orderBooks(orderTO));
        }
    }
}
