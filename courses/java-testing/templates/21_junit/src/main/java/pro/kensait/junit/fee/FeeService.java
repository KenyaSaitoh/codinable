package pro.kensait.junit.fee;

/*
 * 配送料計算サービスを表すクラス（テスト対象）
 */
public class FeeService {
    private static final String STANDARD_REGION_CODE = "R01"; // 地域コード（通常地域）

    // 配送料の計算を行う
    public int calcFee(String regionCode, int amount) {
        // 配送先が通常地域か判定する
        if (STANDARD_REGION_CODE.equals(regionCode)) {
            // 通常地域の場合、3000円以上は無料、それ以外は500円
            if (3000 <= amount) {
                return 0;
            } else {
                return 500;
            }

        } else {
            // 遠隔地域の場合、5000円以上は500円、それ以外は1000円
            if (5000 <= amount) {
                return 500;
            } else {
                return 1000;
            }
        }
    }
}
