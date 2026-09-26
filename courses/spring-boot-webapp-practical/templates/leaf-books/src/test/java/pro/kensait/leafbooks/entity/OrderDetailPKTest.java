package pro.kensait.leafbooks.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/*
 * 注文詳細主キーのテスト
 */
@DisplayName("OrderDetailPKクラスのテスト")
class OrderDetailPKTest {
    // 「Integerキャッシュ外の等価キーによるハッシュ操作」の検証
    @Test
    void equalKeysOutsideIntegerCacheWorkInHashCollections() {
        OrderDetailPK first = new OrderDetailPK(1000, 2000);
        OrderDetailPK second = new OrderDetailPK(1000, 2000);
        assertEquals(first, second);
        assertTrue(java.util.Set.of(first).contains(second));
    }

    // 「未初期化キーのequals・hashCode整合性」の検証
    @Test
    void uninitializedKeysHaveConsistentEqualityAndHashCode() {
        OrderDetailPK first = new OrderDetailPK();
        OrderDetailPK second = new OrderDetailPK();
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertFalse(first.equals(null));
    }

    
    // 「同じ値を持つインスタンスがequalsでtrueになること」の検証
    @Test
    @DisplayName("同じ値を持つインスタンスがequalsでtrueになること")
    void test_equalsReturnsTrueForSameValues() {
        // 準備フェーズ：同じ値を持つ複合主キーを2つ生成する
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        OrderDetailPK pk2 = new OrderDetailPK(1, 1);
        
        // 検証フェーズ：両方向でequalsがtrueを返すことを検証する
        assertTrue(pk1.equals(pk2));
        assertTrue(pk2.equals(pk1));
    }
    
    // 「異なる値を持つインスタンスがequalsでfalseになること」の検証
    @Test
    @DisplayName("異なる値を持つインスタンスがequalsでfalseになること")
    void test_equalsReturnsFalseForDifferentValues() {
        // 準備フェーズ：異なる値を持つ複合主キーを生成する
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        OrderDetailPK pk2 = new OrderDetailPK(1, 2);
        OrderDetailPK pk3 = new OrderDetailPK(2, 1);
        
        // 検証フェーズ：equalsがfalseを返すことを検証する
        assertFalse(pk1.equals(pk2));
        assertFalse(pk1.equals(pk3));
    }
    
    // 「同じ値を持つインスタンスが同じhashCodeを返すこと」の検証
    @Test
    @DisplayName("同じ値を持つインスタンスが同じhashCodeを返すこと")
    void test_hashCodeConsistentForSameValues() {
        // 準備フェーズ：同じ値を持つ複合主キーを2つ生成する
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        OrderDetailPK pk2 = new OrderDetailPK(1, 1);
        
        // 検証フェーズ：hashCodeが一致することを検証する
        assertEquals(pk1.hashCode(), pk2.hashCode());
    }
    
    // 「異なる値を持つインスタンスが異なるhashCodeを返すこと」の検証
    @Test
    @DisplayName("異なる値を持つインスタンスが異なるhashCodeを返すこと")
    void test_hashCodeDifferentForDifferentValues() {
        // 準備フェーズ：異なる値を持つ複合主キーを生成する
        OrderDetailPK pk1 = new OrderDetailPK(1, 1);
        OrderDetailPK pk2 = new OrderDetailPK(1, 2);
        
        // 検証フェーズ：hashCodeが異なることを検証する
        assertNotEquals(pk1.hashCode(), pk2.hashCode());
    }
}
