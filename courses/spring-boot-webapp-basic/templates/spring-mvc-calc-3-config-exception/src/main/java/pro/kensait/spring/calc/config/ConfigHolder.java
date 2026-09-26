package pro.kensait.spring.calc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/*
 * 設定保持の機能を提供するクラス
 */
@Component
@PropertySource("classpath:config.properties")
public class ConfigHolder {
    // 計算結果の小数点以下桁数
    @Value("${calc.result.scale}")
    private Integer calcResultScale;

    // 計算結果の極度
    @Value("${calc.result.limit}")
    private Integer calcResultLimit;

    // 計算結果小数桁の取得
    public Integer getCalcResultScale() {
        return calcResultScale;
    }

    // 計算結果小数桁の設定
    public void setCalcResultScale(Integer calcResultScale) {
        this.calcResultScale = calcResultScale;
    }

    // 計算結果上限の取得
    public Integer getCalcResultLimit() {
        return calcResultLimit;
    }

    // 計算結果上限の設定
    public void setCalcResultLimit(Integer calcResultLimit) {
        this.calcResultLimit = calcResultLimit;
    }
}
