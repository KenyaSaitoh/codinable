package pro.kensait.junit.fee;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/*
 * FeeService（配送料計算サービス）を対象にしたテストクラス
 * @CsvFileSourceに指定されたCSVファイルから、パラメータを読み込む
 * その時、アサンプションによってパラメータをフィルタリングする
 */
@DisplayName("配送料計算サービスのテストクラス4")
public class FeeParameterTest_4 {
    private static final String STANDARD_REGION_CODE = "R01"; // 通常地域

    @ParameterizedTest(name = "順番:{index} ; 引数:{arguments}")
    @CsvFileSource(resources = "/parameter.csv", numLinesToSkip = 1)
    @DisplayName("CSVファイルからパラメータを取得するテストメソッド")
    void test_CalcFee(String regionCode, int amount, int expectedFee) {
        // アサンプションによって、パラメータとして与えられる地域コードを、
        // 「通常地域」にフィルタリングする
        assumeTrue(regionCode.equals(STANDARD_REGION_CODE));

        // テスト続行
        FeeService feeService = new FeeService();
        int actualFee = feeService.calcFee(regionCode, amount);
        assertEquals(expectedFee, actualFee);
    }
}