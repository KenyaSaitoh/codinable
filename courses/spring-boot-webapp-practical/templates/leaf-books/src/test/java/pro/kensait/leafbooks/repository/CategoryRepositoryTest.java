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

import pro.kensait.leafbooks.entity.Category;

/*
 * カテゴリのテスト
 */
@DataJpaTest
@Sql(scripts = "/test-data.sql")
@DisplayName("CategoryRepositoryのテスト")
class CategoryRepositoryTest {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    // 「IDでカテゴリを検索できること」の検証
    @Test
    @DisplayName("IDでカテゴリを検索できること")
    void test_findById() {
        // 実行フェーズ：IDでカテゴリを検索する
        Optional<Category> found = categoryRepository.findById(1);
        
        // 検証フェーズ：期待値と実測値が一致しているかを検証する
        assertTrue(found.isPresent());
        assertEquals("Java", found.get().getCategoryName());
    }
    
    // 「全カテゴリを取得できること」の検証
    @Test
    @DisplayName("全カテゴリを取得できること")
    void test_findAll() {
        // 実行フェーズ：全カテゴリを取得する
        List<Category> categories = categoryRepository.findAll();
        
        // 検証フェーズ：期待値と実測値が一致しているかを検証する
        assertNotNull(categories);
        assertTrue(categories.size() >= 9);
    }
}
