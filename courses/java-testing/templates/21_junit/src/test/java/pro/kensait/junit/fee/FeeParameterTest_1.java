package pro.kensait.junit.fee;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/*
 * FeeService（配送料計算サービス）を対象にしたテストクラス
 * @ValueSourceからアノテーションから単一のパラメータを読み込む
 */
@DisplayName("配送料計算サービスのテストクラス1")
public class FeeParameterTest_1 {

    @ParameterizedTest(name = "順番:{index} ; 引数:{arguments}")
    @ValueSource(strings = {"R01,3000,0", "R01,2999,500", "R99,5000,500", "R99,4999,1000"})
    @DisplayName("リテラル配列からパラメータを取得するテストメソッド")
    void test_CalcFee(String paramStr) {
        String[] param = paramStr.split(",");
        FeeService feeService = new FeeService();
        // 実行フェーズ
        int actualFee = feeService.calcFee(param[0], Integer.parseInt(param[1]));
        // 検証フェーズ
        assertEquals(Integer.parseInt(param[2]), actualFee);
    }
}