package pro.kensait.spring.calc.web;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import pro.kensait.spring.calc.service.CalcService;
import pro.kensait.spring.calc.service.exception.LimitOverException;
import pro.kensait.spring.calc.service.exception.ZeroDivideException;

/*
 * 計算機能のコントローラーを担うクラス
 */
@Controller
public class CalcController {
    private static final Logger LOGGER = LoggerFactory.getLogger(
            CalcController.class);

    // インジェクションポイント
    @Autowired
    private CalcService calcService;

    // インジェクションポイント
    @Autowired
    private MessageSource messageSource;

    // アクションメソッド：入力画面への遷移
    @GetMapping("/")
    public String index(@ModelAttribute("calcParam") CalcParam calcParam) {
        LOGGER.info("[ CalcController#index ]");
        return "CalcInputPage";
    }

    // アクションメソッド：足し算の実行
    @PostMapping("/add")
    // BindingResultは、必ずパラメータの直後の引数に指定する
    public String add(@Validated CalcParam calcParam, BindingResult errors,
            Model model) {

        LOGGER.info("[ CalcController#add ]");

        rejectZero(calcParam.param1(), "param1", "パラメータ1", errors);
        rejectZero(calcParam.param2(), "param2", "パラメータ2", errors);

        // 入力値検証の結果を調べ、エラー発生時は入力画面へ遷移する
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }

        // ビジネスロジックを呼び出す
        BigDecimal result = calcService.add(calcParam.param1(), calcParam.param2());

        // 計算結果をModelに格納する
        model.addAttribute("result", result);

        // 遷移先ページ（結果画面）の論理名を返却する
        return "CalcOutputPage";
    }

    // アクションメソッド：引き算の実行
    @PostMapping("/subtract")
    public String subtract(@Validated CalcParam calcParam, BindingResult errors,
            Model model) {

        LOGGER.info("[ CalcController#subtract ]");

        // 2つのパラメータの値が同一だった場合はエラーにする
        // → グローバルエラーを追加し、元の画面に遷移する
        if (calcParam.param1().equals(calcParam.param2())) {
            ObjectError error = new ObjectError("globarError",
                    new String[]{"error.same.value"}, null, null);
            errors.addError(error);
        }

        // 入力値検証の結果を調べ、エラー発生時は入力画面へ遷移する
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }

        // ビジネスロジックを呼び出す
        BigDecimal result = calcService.subtract(calcParam.param1(), calcParam.param2());

        // 計算結果をModelに格納する
        model.addAttribute("result", result);

        // 遷移先ページ（結果画面）の論理名を返却する
        return "CalcOutputPage";
    }

    // アクションメソッド：掛け算の実行
    @PostMapping("/multiply")
    public String multiply(@Validated CalcParam calcParam, BindingResult errors,
            Model model) {

        LOGGER.info("[ CalcController#multiply ]");

        // 入力値検証の結果を調べ、エラー発生時は入力画面へ遷移する
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }

        // ビジネスロジックを呼び出す
        BigDecimal result = null;
        try {
            result = calcService.multiply(calcParam.param1(),
                    calcParam.param2());
        } catch (LimitOverException exception) {
            return limitOver(calcParam, model, exception);
        }

        // 計算結果をModelに格納する
        model.addAttribute("result", result);

        // 遷移先ページ（結果画面）の論理名を返却する
        return "CalcOutputPage";
    }

    // アクションメソッド：割り算の実行
    @PostMapping("/divide")
    public String divide(@Validated CalcParam calcParam, BindingResult errors,
            Model model) {

        LOGGER.info("[ CalcController#divide ]");

        // 入力値検証の結果を調べ、エラー発生時は入力画面へ遷移する
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }

        // ビジネスロジックを呼び出す
        BigDecimal result = null;
        try {
            result = calcService.divide(calcParam.param1(),
                    calcParam.param2());
        } catch (ZeroDivideException exception) {
            return zeroDivide(model, exception);
        }

        // 計算結果をModelに格納する
        model.addAttribute("result", result);

        // 遷移先ページ（結果画面）の論理名を返却する
        return "CalcOutputPage";
    }

    // rejectゼロの実行
    private void rejectZero(BigDecimal value, String field, String label,
            BindingResult errors) {
        if (value != null && value.compareTo(BigDecimal.ZERO) == 0) {
            LOGGER.info("[ CalcController#add ] ゼロ加算エラー");
            errors.rejectValue(field, "error.zero.value", label);
        }
    }

    // 上限超過の実行
    private String limitOver(CalcParam calcParam, Model model,
            LimitOverException exception) {
        LOGGER.error("極度オーバーが発生しました", exception);
        String errorMessage = messageSource.getMessage("error.limit.over",
                new String[] {calcParam.param1().toString(),
                        calcParam.param2().toString()}, null);
        model.addAttribute("errorMessage", errorMessage);
        return "CalcErrorPage";
    }

    // ゼロdivideの実行
    private String zeroDivide(Model model, ZeroDivideException exception) {
        LOGGER.error("ゼロ割が発生しました", exception);
        String errorMessage = messageSource.getMessage("error.zero.divide", null, null);
        model.addAttribute("errorMessage", errorMessage);
        return "CalcErrorPage";
    }
}
