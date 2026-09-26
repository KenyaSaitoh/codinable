package pro.kensait.leafbooks.service.category;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pro.kensait.leafbooks.entity.Category;
import pro.kensait.leafbooks.repository.CategoryRepository;

/*
 * カテゴリのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceのテスト")
class CategoryServiceTest {
    
    // テスト対象クラス
    @InjectMocks
    private CategoryService categoryService;
    
    // テスト対象クラスの呼び出し先
    @Mock
    private CategoryRepository categoryRepository;
    
    // すべてのテストケースで共通的なフィクスチャ
    private List<Category> testCategories;
    
    // 各テストケースで共通的な前処理
    @BeforeEach
    void setUp() {
        Category category1 = new Category(1, "Java");
        Category category2 = new Category(2, "SpringBoot");
        Category category3 = new Category(3, "SQL");
        testCategories = Arrays.asList(category1, category2, category3);
    }
    
    // 「カテゴリマップを取得できること」の検証
    @Test
    @DisplayName("カテゴリマップを取得できること")
    void test_getCategoryMap() {
        // 準備フェーズ：CategoryRepositoryのモック動作を設定する
        when(categoryRepository.findAll()).thenReturn(testCategories);
        
        // 実行フェーズ：カテゴリマップを取得する
        Map<String, Integer> result = categoryService.getCategoryMap();
        
        // 検証フェーズ：期待値と実測値が一致しているかを検証する
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(1, result.get("Java"));
        assertEquals(2, result.get("SpringBoot"));
        assertEquals(3, result.get("SQL"));
        verify(categoryRepository, times(1)).findAll();
    }
}
