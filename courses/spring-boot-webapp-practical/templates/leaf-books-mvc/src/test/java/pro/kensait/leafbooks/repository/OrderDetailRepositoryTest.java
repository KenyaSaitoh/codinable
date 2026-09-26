package pro.kensait.leafbooks.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import pro.kensait.leafbooks.entity.OrderDetail;
import pro.kensait.leafbooks.entity.OrderDetailPK;

/*
 * 注文詳細のテスト
 */
@DataJpaTest
@Sql(scripts = "/test-data.sql")
@DisplayName("OrderDetailRepositoryのテスト")
class OrderDetailRepositoryTest {
    
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    
    // 「複合主キーで注文明細を検索できること」の検証
    @Test
    @DisplayName("複合主キーで注文明細を検索できること")
    void test_findById() {
        // 準備フェーズ：複合主キーを生成する
        OrderDetailPK pk = new OrderDetailPK(1, 1);
        
        // 実行フェーズ：複合主キーで注文明細を検索する
        Optional<OrderDetail> found = orderDetailRepository.findById(pk);
        
        // 検証フェーズ：期待値と実測値が一致しているかを検証する
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getCount());
        assertEquals(3400, found.get().getPrice().intValue());
    }
}
