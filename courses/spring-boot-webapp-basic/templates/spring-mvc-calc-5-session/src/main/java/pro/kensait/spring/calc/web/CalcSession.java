package pro.kensait.spring.calc.web;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/*
 * 計算処理のセッション
 */
public class CalcSession {
    // パラメータ1
    @NotNull @Min(-1000) @Max(1000)
    private BigDecimal param1;

    // パラメータ2
    @NotNull @Min(-1000) @Max(1000)
    private BigDecimal param2;

    // 計算結果
    private BigDecimal result;

    // アクセサメソッド
    public BigDecimal getParam1() {
        return param1;
    }

    // パラメータ1の設定
    public void setParam1(BigDecimal param1) {
        this.param1 = param1;
    }

    // パラメータ2の取得
    public BigDecimal getParam2() {
        return param2;
    }

    // パラメータ2の設定
    public void setParam2(BigDecimal param2) {
        this.param2 = param2;
    }

    // 結果の取得
    public BigDecimal getResult() {
        return result;
    }

    // 結果の設定
    public void setResult(BigDecimal result) {
        this.result = result;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "CalcSession [param1=" + param1 + ", param2=" + param2 + ", result="
                + result + "]";
    }
}
