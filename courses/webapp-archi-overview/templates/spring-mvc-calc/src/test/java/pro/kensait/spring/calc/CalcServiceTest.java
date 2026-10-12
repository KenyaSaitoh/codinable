package pro.kensait.spring.calc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/*
 * CalcService の単体テスト
 * Spring を起動せず、素の Java オブジェクトとして試せる
 *
 * 実行対象で「Gradle: test」を選ぶと、テスト結果タブに一覧が出る
 */
class CalcServiceTest {

    private final CalcService calcService = new CalcService();

    @Test
    @DisplayName("足し算ができる")
    void testAdd() {
        // 小数の比較には許容誤差 (第3引数) を指定する
        assertEquals(30.0, calcService.add(10.0, 20.0), 0.0001);
    }

    @Test
    @DisplayName("引き算ができる")
    void testSubtract() {
        assertEquals(-10.0, calcService.subtract(10.0, 20.0), 0.0001);
    }

    @Test
    @DisplayName("掛け算ができる")
    void testMultiply() {
        assertEquals(200.0, calcService.multiply(10.0, 20.0), 0.0001);
    }

    @Test
    @DisplayName("割り算ができる")
    void testDivide() {
        assertEquals(0.5, calcService.divide(10.0, 20.0), 0.0001);
    }

    @Test
    @DisplayName("0 で割ると例外になる")
    void testDivideByZero() {
        assertThrows(IllegalArgumentException.class, () -> calcService.divide(10.0, 0.0));
    }

    @Test
    @DisplayName("計算すると新しい順に履歴が残る")
    void testHistory() {
        calcService.add(10.0, 20.0);
        calcService.multiply(2.0, 3.0);
        assertEquals(List.of("2.0 × 3.0 = 6.0", "10.0 + 20.0 = 30.0"), calcService.getHistory(10));
    }

    @Test
    @DisplayName("履歴は limit 件までに絞れる")
    void testHistoryLimit() {
        calcService.add(1.0, 1.0);
        calcService.add(2.0, 2.0);
        calcService.add(3.0, 3.0);
        assertEquals(List.of("3.0 + 3.0 = 6.0", "2.0 + 2.0 = 4.0"), calcService.getHistory(2));
    }

    @Test
    @DisplayName("0 除算は履歴に残らない")
    void testDivideByZeroNotRecorded() {
        assertThrows(IllegalArgumentException.class, () -> calcService.divide(10.0, 0.0));
        assertEquals(List.of(), calcService.getHistory(10));
    }
}
