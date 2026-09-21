package pro.kensait.spring.calc;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/*
 * フォームから送られてくるパラメータ
 *
 * アノテーションが入力値の決まりを表し、Controller 側で @Validated を付けると
 * Spring が送信値を検査してくれる。エラーメッセージは
 * src/main/resources/ValidationMessages.properties にある
 */
public record CalcParam(
        @NotNull
        @Min(-1000)
        @Max(1000)
        Double param1,

        @NotNull
        @Min(-1000)
        @Max(1000)
        Double param2) {
}
