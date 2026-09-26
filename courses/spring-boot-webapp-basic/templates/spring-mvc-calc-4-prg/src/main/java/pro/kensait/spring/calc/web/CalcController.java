package pro.kensait.spring.calc.web;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pro.kensait.spring.calc.service.CalcService;

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
            RedirectAttributes redirectAttributes) {

        LOGGER.info("[ CalcController#add ]");

        // 入力値検証の結果を調べ、エラー発生時は入力画面へ遷移する
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }

        // ビジネスロジックを呼び出す
        BigDecimal result = calcService.add(calcParam.param1(), calcParam.param2());

        // 計算結果をフラッシュスコープに格納する
        redirectAttributes.addFlashAttribute("result", result);

        // 結果画面にリダイレクトする
        return "redirect:/viewAddResult";
    }

    // アクションメソッド：結果画面（足し算）への遷移
    @GetMapping("/viewAddResult")
    public String viewAddResult(Model model) {
        LOGGER.info("[ CalcController#viewAddResult ]");

        // フラッシュスコープから計算結果を取得する
        BigDecimal result = (BigDecimal) model.getAttribute("result");

        // 計算結果をModelに格納する
        model.addAttribute("value", result);
        return "CalcOutputPage";
    }

    // アクションメソッド：引き算の実行
    @PostMapping("/subtract")
    public String subtract(@Validated CalcParam calcParam, BindingResult errors,
            RedirectAttributes redirectAttributes) {

        LOGGER.info("[ CalcController#subtract ]");

        // 入力値検証の結果を調べ、エラー発生時は入力画面へ遷移する
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }

        // ビジネスロジックを呼び出す
        BigDecimal result = calcService.subtract(calcParam.param1(), calcParam.param2());

        // 計算結果をフラッシュスコープに格納する
        redirectAttributes.addFlashAttribute("result", result);

        // 結果画面にリダイレクトする
        return "redirect:/viewSubtractResult";
    }

    // アクションメソッド：結果画面（引き算）への遷移
    @GetMapping("/viewSubtractResult")
    public String viewSubtractResult(Model model) {
        LOGGER.info("[ CalcController#viewSubtractResult ]");

        // フラッシュスコープから計算結果を取得する
        BigDecimal result = (BigDecimal) model.getAttribute("result");

        // 計算結果をModelに格納する
        model.addAttribute("value", result);
        return "CalcOutputPage";
    }
}
