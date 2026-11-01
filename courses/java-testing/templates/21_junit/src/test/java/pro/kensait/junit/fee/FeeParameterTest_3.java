package pro.kensait.junit.fee;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/*
 * FeeService（配送料計算サービス）を対象にしたテストクラス
 * @MethodSourceに指定されたスタティックメソッドの呼び出し結果から、パラメータを読み込む
 */
@DisplayName("配送料計算サービスのテストクラス3")
public class FeeParameterTest_3 {
    private static final String STANDARD_REGION_CODE = "R01"; // 通常地域
    private static final String REMOTE_REGION_CODE = "R99"; // 遠隔地域

    @ParameterizedTest(name = "順番:{index} ; 引数:{arguments}")
    @MethodSource("paramProvider")
    @DisplayName("スタティックメソッドからパラメータを取得するテストメソッド")
    void test_CalcFee(FeeParam param) {
        FeeService feeService = new FeeService();
        int actualFee = feeService.calcFee(param.regionCode, param.amount);
        assertEquals(param.expectedFee, actualFee);
    }

    // パラメータを提供するスタティックメソッド
    static Stream<FeeParam> paramProvider() {
        return Stream.of(
            new FeeParam(STANDARD_REGION_CODE, 3999, 0),
            new FeeParam(STANDARD_REGION_CODE, 2000, 500),
            new FeeParam(REMOTE_REGION_CODE, 5000, 500),
            new FeeParam(REMOTE_REGION_CODE, 4999, 1000)
        );
    }

    // パラメータを表す入れ子クラス
    static class FeeParam {
        String regionCode; // 地域コード
        int amount; // 購入金額
        int expectedFee; // 期待値（配送料）
        FeeParam(String regionCode, int amount, int expectedFee) {
            this.regionCode = regionCode;
            this.amount = amount;
            this.expectedFee = expectedFee;
        }
        @Override
        public String toString() {
            return "FeeParam [regionCode=" + regionCode + ", amount=" + amount + ", expectedFee="
                    + expectedFee + "]";
        }
    }
}