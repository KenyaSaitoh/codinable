package pro.kensait.spring.calc.web;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pro.kensait.spring.calc.service.CalcResult;
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
    // BindingResultはパラメータの直後の引数に指定する
    public String add(@Validated CalcParam calcParam, BindingResult errors,
            RedirectAttributes attributes) {
        LOGGER.info("[ CalcController#add ]");

        // 入力値検証の結果を調べ、エラー発生時は入力画面へ遷移する
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }

        // ビジネスロジックを呼び出す
        int id = calcService.add(calcParam.param1(), calcParam.param2());

        // 計算結果IDをRedirectAttributesに格納する（引き回し用）
        attributes.addAttribute("id", id);

        // 遷移先ページの論理名を返却する
        // 先頭に"redirect:"と記載することで、当該ページにリダイレクトさせることができる
        return "redirect:/viewResult";
    }

    // アクションメソッド：引き算の実行
    @PostMapping("/subtract")
    public String subtract(@Validated CalcParam calcParam, BindingResult errors,
            RedirectAttributes attributes) {
        LOGGER.info("[ CalcController#subtract ]");

        // 入力値検証の結果を調べ、エラー発生時は入力画面へ遷移する
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }

        // ビジネスロジックを呼び出す
        int id = calcService.subtract(calcParam.param1(), calcParam.param2());

        // 計算結果IDをRedirectAttributesに格納する（引き回し用）
        attributes.addAttribute("id", id);

        // 遷移先ページの論理名を返却する
        // 先頭に"redirect:"と記載することで、当該ページにリダイレクトさせることができる
        return "redirect:/viewResult";
    }

    // アクションメソッド：掛け算の実行
    @PostMapping("/multiply")
    public String multiply(@Validated CalcParam calcParam, BindingResult errors,
            Model model, RedirectAttributes attributes) {
        LOGGER.info("[ CalcController#multiply ]");

        // 入力値検証の結果を調べ、エラー発生時は入力画面へ遷移する
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }

        // ビジネスロジックを呼び出す
        int id = 0;
        try {
            id = calcService.multiply(calcParam.param1(), calcParam.param2());

        } catch (LimitOverException exception) {
            return limitOver(calcParam, model, exception);
        }

        // 計算結果IDをRedirectAttributesに格納する（引き回し用）
        attributes.addAttribute("id", id);

        // 遷移先ページの論理名を返却する
        // 先頭に"redirect:"と記載することで、当該ページにリダイレクトさせることができる
        return "redirect:/viewResult";
    }

    // アクションメソッド：割り算の実行
    @PostMapping("/divide")
    public String divide(@Validated CalcParam calcParam, BindingResult errors,
            Model model, RedirectAttributes attributes) {

        LOGGER.info("[ CalcController#divide ]");

        // 入力値検証の結果を調べ、エラー発生時は入力画面へ遷移する
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }

        // ビジネスロジックを呼び出す
        int id = 0;
        try {
            id = calcService.divide(calcParam.param1(), calcParam.param2());

        } catch (ZeroDivideException exception) {
            return zeroDivide(model, exception);
        }

        // 計算結果IDをRedirectAttributesに格納する（引き回し用）
        attributes.addAttribute("id", id);

        // 遷移先ページの論理名を返却する
        // 先頭に"redirect:"と記載することで、当該ページにリダイレクトさせることができる
        return "redirect:/viewResult";
    }

    // アクションメソッド：計算結果の表示
    @GetMapping("/viewResult")
    public String viewResult(
            @RequestParam(name = "id", required = true) Integer id,
            Model model) {
        LOGGER.info("[ CalcController#viewResult ] ID => " + id);

        // ビジネスロジックを呼び出し、計算結果を取得する
        CalcResult calcResult = calcService.getCalcResult(id);

        // 計算結果をModelに格納する
        model.addAttribute("calcResult", calcResult);

        // 遷移先ページの論理名を返却する
        return "CalcOutputPage";
    }

    // アクションメソッド：履歴の表示
    @GetMapping("/viewHistory1")
    public String viewHistory1(Model model) {
        LOGGER.info("[ CalcController#viewHistory1 ]");

        // ビジネスロジックを呼び出し、計算結果を取得する
        List<CalcResult> calcResults = calcService.getCalcResults();

        // 計算結果をModelに格納する
        model.addAttribute("calcResults", calcResults);

        // 遷移先ページの論理名を返却する
        return "CalcHistoryPage1";
    }

    // アクションメソッド：履歴の表示
    @GetMapping("/viewHistory2")
    public String viewHistory2(Model model) {
        LOGGER.info("[ CalcController#viewHistory2 ]");

        // ビジネスロジックを呼び出し、計算結果リストを取得する
        List<CalcResult> calcResults = calcService.getCalcResults();

        // 計算結果リストをModelに格納する
        model.addAttribute("calcResults", calcResults);

        // 遷移先ページの論理名を返却する
        return "CalcHistoryPage2";
    }

    // view結果によるクエリの実行
    @GetMapping("/viewResultByQuery")
    public String viewResultByQuery(
            @RequestParam(name = "id", required = false) int id,
            Model model) {

        // ビジネスロジックを呼び出す
        CalcResult calcResult = calcService.getCalcResult(id);

        // 取得結果をModelに格納する
        model.addAttribute("calcResult", calcResult);
        return "CalcDetailPage";
    }

    // アクションメソッド：履歴の表示
    @GetMapping("/viewHistory3")
    public String viewHistory3(Model model) {
        LOGGER.info("[ CalcController#viewHistory3 ]");

        // ビジネスロジックを呼び出し、計算結果リストを取得する
        List<CalcResult> calcResults = calcService.getCalcResults();

        // 計算結果リストをModelに格納する
        model.addAttribute("calcResults", calcResults);

        // 遷移先ページの論理名を返却する
        return "CalcHistoryPage3";
    }

    // view結果によるpathの実行
    @GetMapping("/viewResultByPath/{id}")
    public String viewResultByPath(@PathVariable(name = "id") int id,
            Model model) {

        // ビジネスロジックを呼び出す
        CalcResult calcResult = calcService.getCalcResult(id);

        // 取得結果をModelに格納する
        model.addAttribute("calcResult", calcResult);
        return "CalcDetailPage";
    }

    // 上限超過の実行
    private String limitOver(CalcParam calcParam, Model model,
            LimitOverException exception) {
        LOGGER.error("極度オーバーが発生しました", exception);
        String errorMessage = messageSource.getMessage("error.limit.over",
                new String[] {calcParam.param1().toString(),
                        calcParam.param2().toString()}, Locale.JAPANESE);
        model.addAttribute("errorMessage", errorMessage);
        return "CalcErrorPage";
    }

    // ゼロdivideの実行
    private String zeroDivide(Model model, ZeroDivideException exception) {
        LOGGER.error("ゼロ割が発生しました", exception);
        String errorMessage = messageSource.getMessage("error.zero.divide", null,
                Locale.JAPANESE);
        model.addAttribute("errorMessage", errorMessage);
        return "CalcErrorPage";
    }
}
