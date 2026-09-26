package pro.kensait.leafbooks.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/*
 * ユーティリティのテスト
 */
@DisplayName("Utilクラスのテスト")
class UtilTest {
    
    // 「sleepRandomメソッドが指定範囲内でスリープすること」の検証
    @Test
    @DisplayName("sleepRandomメソッドが指定範囲内でスリープすること")
    void test_sleepRandomWithinRange() {
        // 準備フェーズ：スリープ時間の範囲を設定する
        long from = 10;
        long to = 20;
        long startTime = System.currentTimeMillis();
        
        // 実行フェーズ：ランダムなスリープを実行する
        Util.sleepRandom(from, to);
        
        // 検証フェーズ：スリープ時間が指定範囲内であることを検証する
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        assertTrue(duration >= from - 5, "スリープ時間が短すぎます: " + duration);
        assertTrue(duration <= to + 50, "スリープ時間が長すぎます: " + duration);
    }
}
