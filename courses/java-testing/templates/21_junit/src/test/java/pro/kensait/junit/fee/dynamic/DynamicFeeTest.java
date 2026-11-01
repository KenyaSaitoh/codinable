package pro.kensait.junit.fee.dynamic;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import pro.kensait.junit.fee.FeeService;

/*
 * FeeService（配送料計算サービス）を対象にしたテストクラス
 * 動的テストによって実行時にテストケースを動的に生成する
 */
public class DynamicFeeTest {
    private static final String STANDARD_REGION_CODE = "R01"; // 通常地域
    private static final String REMOTE_REGION_CODE = "R99"; // 遠隔地域

    // テスト対象クラス
    FeeService feeService;

    // 各テストメソッドで共通的な前処理
    @BeforeEach
    void setUp() {
        // 共通フィクスチャを設定する
        feeService = new FeeService();
    }

    @TestFactory
    Collection<DynamicTest> dynamicFeeTests() {
        return Arrays.asList(
                DynamicTest.dynamicTest("通常地域に3000円の購入、"
                        + "配送料が0円であることをテストする", () -> {
                    assertEquals(0, feeService.calcFee(STANDARD_REGION_CODE, 3000));
                }),
                DynamicTest.dynamicTest("通常地域に2999円の購入、"
                        + "配送料が500円であることをテストする", () -> {
                    assertEquals(500, feeService.calcFee(STANDARD_REGION_CODE, 2999));
                }),
                DynamicTest.dynamicTest("遠隔地域に5000円の購入、"
                        + "配送料が500円であることをテストする", () -> {
                    assertEquals(500, feeService.calcFee(REMOTE_REGION_CODE, 5000));
                }),
                DynamicTest.dynamicTest("遠隔地域に4999円の購入、"
                        + "配送料が1000円であることをテストする", () -> {
                    assertEquals(1000, feeService.calcFee(REMOTE_REGION_CODE, 4999));
                }));
    }
}