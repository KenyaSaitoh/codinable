package pro.kensait.junit.fee;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/*
 * FeeService（配送料計算サービス）を対象にしたテストクラス
 * 期待値は仕様の表から書き写し、境界の両側を 1 メソッドずつ確かめる
 */
public class FeeServiceTest {
    public static final String STANDARD_REGION_CODE = "R01"; // 通常地域
    public static final String REMOTE_REGION_CODE = "R99"; // 遠隔地域

    // テスト対象クラス
    FeeService feeService;

    // 各テストメソッドで共通的な前処理
    @BeforeEach
    void setUp() {
        // 共通フィクスチャを設定する
        feeService = new FeeService();
    }

    @Test
    @DisplayName("通常地域に3000円の購入、配送料が0円であることをテストする")
    void test_CalcFee_Standard_Over3000_RightFee() {
        int actual = feeService.calcFee(STANDARD_REGION_CODE, 3000); // 【1】
        assertEquals(0, actual); // 【2】
    }

    @Test
    @DisplayName("通常地域に2999円の購入、配送料が500円であることをテストする")
    void test_CalcFee_Standard_Under3000_RightFee() {
        int actual = feeService.calcFee(STANDARD_REGION_CODE, 2999);
        assertEquals(500, actual);
    }

    @Test
    @DisplayName("遠隔地域に5000円の購入、配送料が500円であることをテストする")
    void test_CalcFee_Remote_Over5000_RightFee() {
        int actual = feeService.calcFee(REMOTE_REGION_CODE, 5000);
        assertEquals(500, actual);
    }

    @Test
    @DisplayName("遠隔地域に4999円の購入、配送料が1000円であることをテストする")
    void test_CalcFee_Remote_Under5000_RightFee() {
        int actual = feeService.calcFee(REMOTE_REGION_CODE, 4999);
        assertEquals(1000, actual);
    }
}
