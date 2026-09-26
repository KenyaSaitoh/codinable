package pro.kensait.leafbooks.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import pro.kensait.leafbooks.entity.Stock;

/*
 * 在庫のテスト
 */
@DataJpaTest
@Sql(scripts = "/test-data.sql")
@DisplayName("StockRepositoryのテスト")
class StockRepositoryTest {
    
    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private StockRepository stockRepository;
    
    // 「IDで在庫を検索できること」の検証
    @Test
    @DisplayName("IDで在庫を検索できること")
    void test_findById() {
        // 実行フェーズ：IDで在庫を検索する
        Optional<Stock> found = stockRepository.findById(1);
        
        // 検証フェーズ：期待値と実測値が一致しているかを検証する
        assertTrue(found.isPresent());
        assertEquals(3, found.get().getQuantity());
        assertEquals(0L, found.get().getVersion());
    }
    
    // 「楽観的ロック付きで在庫数を更新できること」の検証
    @Test
    @DisplayName("楽観的ロック付きで在庫数を更新できること")
    void test_updateQuantityWithOptimisticLock() {
        // 準備フェーズ：現在の在庫情報を取得する
        Stock currentStock = stockRepository.findById(1).orElseThrow();
        Long currentVersion = currentStock.getVersion();
        Integer currentQuantity = currentStock.getQuantity();
        
        // 実行フェーズ：楽観的ロック付きで在庫数を更新する
        Integer updateCount = stockRepository.updateQuantity(1, 1, currentVersion);
        entityManager.flush();
        entityManager.clear();
        
        // 検証フェーズ：更新が成功し、在庫数とバージョンが正しく更新されたかを検証する
        assertEquals(1, updateCount);
        
        Stock updatedStock = stockRepository.findById(1).orElseThrow();
        assertEquals(currentQuantity - 1, updatedStock.getQuantity());
        assertEquals(currentVersion + 1, updatedStock.getVersion());
    }
    
    // 「バージョンが一致しない場合、更新が失敗すること」の検証
    @Test
    @DisplayName("バージョンが一致しない場合、更新が失敗すること")
    void test_updateQuantityFailsWithVersionMismatch() {
        // 準備フェーズ：存在しない不正なバージョン番号を設定する
        Long wrongVersion = 999L;
        
        // 実行フェーズ：不正なバージョンで更新を試みる
        Integer updateCount = stockRepository.updateQuantity(1, 1, wrongVersion);
        entityManager.flush();
        
        // 検証フェーズ：更新が失敗し、在庫数が変更されていないことを検証する
        assertEquals(0, updateCount);
        
        Stock unchangedStock = stockRepository.findById(1).orElseThrow();
        assertEquals(3, unchangedStock.getQuantity());
    }
}
