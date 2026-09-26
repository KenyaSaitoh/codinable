package pro.kensait.leafbooks.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import pro.kensait.leafbooks.entity.OrderTran;
import pro.kensait.leafbooks.service.order.OrderHistoryTO;

/*
 * 注文tranのテスト
 */
@DataJpaTest
@Sql(scripts = "/test-data.sql")
@DisplayName("OrderTranRepositoryのテスト")
class OrderTranRepositoryTest {
    
    @Autowired
    private OrderTranRepository orderTranRepository;
    
    // 「IDで注文を検索できること」の検証
    @Test
    @DisplayName("IDで注文を検索できること")
    void test_findById() {
        // 実行フェーズ：IDで注文を検索する
        Optional<OrderTran> found = orderTranRepository.findById(1);
        
        // 検証フェーズ：期待値と実測値が一致しているかを検証する
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getCustomer());
        assertEquals("東京都中央区1-1-1", found.get().getDeliveryAddress());
    }
    
    // 「顧客IDで注文を検索できること」の検証
    @Test
    @DisplayName("顧客IDで注文を検索できること")
    void test_findByCustomer() {
        // 実行フェーズ：顧客IDで注文を検索する
        List<OrderTran> orders = orderTranRepository.findByCustomer(1);
        
        // 検証フェーズ：期待値と実測値が一致しているかを検証する
        assertNotNull(orders);
        assertTrue(orders.size() >= 2);
        assertEquals(1, orders.get(0).getCustomer());
    }
    
    // 「顧客IDで注文履歴TOを検索できること」の検証
    @Test
    @DisplayName("顧客IDで注文履歴TOを検索できること")
    void test_findOrderHistoryTOByCustomer() {
        // 実行フェーズ：顧客IDで注文履歴TOを検索する
        List<OrderHistoryTO> orderHistories = 
                orderTranRepository.findOrderHistoryTOByCustomer(1);
        
        // 検証フェーズ：期待値と実測値が一致しているかを検証する
        assertNotNull(orderHistories);
        assertTrue(orderHistories.size() >= 4);
        assertNotNull(orderHistories.get(0).orderDate());
        assertNotNull(orderHistories.get(0).bookName());
    }
    
    // 「注文明細を含めて注文を取得できること」の検証
    @Test
    @DisplayName("注文明細を含めて注文を取得できること")
    void test_findByIdWithDetails() {
        // 実行フェーズ：注文明細を含めて注文を取得する（fetch join）
        OrderTran found = orderTranRepository.findByIdWithDetails(1);
        
        // 検証フェーズ：期待値と実測値が一致しているかを検証する
        assertNotNull(found);
        assertNotNull(found.getOrderDetails());
        assertEquals(2, found.getOrderDetails().size());
    }
}
