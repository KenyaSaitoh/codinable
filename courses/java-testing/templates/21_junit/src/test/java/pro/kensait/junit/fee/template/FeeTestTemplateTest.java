package pro.kensait.junit.fee.template;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import pro.kensait.junit.fee.FeeService;

/*
 * FeeService（配送料計算サービス）を対象にしたテストクラス
 * テストテンプレートによって動的にパラメータを切り替える
 */
public class FeeTestTemplateTest {

    public static final String STANDARD_REGION_CODE = "R01";
    public static final String REMOTE_REGION_CODE = "R99";

    FeeService feeService;

    @BeforeEach
    void setUp() {
        feeService = new FeeService();
    }

    @TestTemplate
    @ExtendWith(FeeTestTemplateProvider.class)
    @DisplayName("テストテンプレートによる配送料計算のテスト")
    void test_CalcFee(String regionCode, int amount, int expectedFee) {
        int actual = feeService.calcFee(regionCode, amount);
        assertEquals(expectedFee, actual);
    }
}
