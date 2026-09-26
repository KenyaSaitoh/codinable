package pro.kensait.spring.calc.web;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import pro.kensait.spring.calc.service.CalcService;
import pro.kensait.spring.calc.service.TaxService;
import pro.kensait.spring.calc.service.exception.LimitOverException;
import pro.kensait.spring.calc.service.exception.ZeroDivideException;

/*
 * 計算機能のコントローラーを担うクラス
 */
@Controller
@SessionAttributes("calcSession")
public class CalcController {
    private static final Logger LOGGER = LoggerFactory.getLogger(
            CalcController.class);

    // インジェクションポイント
    @Autowired
    private CalcService calcService;

    // インジェクションポイント
    @Autowired
    private TaxService taxService;

    // インジェクションポイント
    @Autowired
    private MessageSource messageSource;

    // initセッションの実行
    @ModelAttribute("calcSession")
    public CalcSession initSession() {
        // セッションスコープなので、初回HTTPリクエスト時にのみ呼び出される
        // ただしsessionStatus.setComplete呼び出しによってcalcSessionに処理終了マークが付くと、
        // また次のHTTPリクエストで呼び出される
        LOGGER.info("[ CalcController#initSession ]");
        return new CalcSession();
    }

    // アクションメソッド：入力画面への遷移
    @GetMapping("/")
    public String index() {
        return "CalcInputPage";
    }

    // アクションメソッド：足し算の実行
    @PostMapping("/add")
    // BindingResultは、必ずパラメータの直後の引数に指定する
    public String add(@Validated CalcSession calcSession, BindingResult errors,
            Model model) {

        LOGGER.info("[ CalcController#add ]");

        // 入力値検証の結果を調べ、エラー発生時は入力画面へ遷移する
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }

        // ビジネスロジックを呼び出す
        BigDecimal result = calcService.add(calcSession.getParam1(),
                calcSession.getParam2());

        // 計算結果をcalcSessionに格納する
        calcSession.setResult(result);

        // 遷移先ページ（結果画面）の論理名を返却する
        return "CalcOutputPage";
    }

    // アクションメソッド：引き算の実行
    @PostMapping("/subtract")
    public String subtract(@Validated CalcSession calcSession, BindingResult errors,
            Model model) {

        LOGGER.info("[ CalcController#subtract ]");

        // 入力値検証の結果を調べ、エラー発生時は入力画面へ遷移する
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }

        // ビジネスロジックを呼び出す
        BigDecimal result = calcService.subtract(calcSession.getParam1(),
                calcSession.getParam2());

        // 計算結果をcalcSessionに格納する
        calcSession.setResult(result);

        // 遷移先ページ（結果画面）の論理名を返却する
        return "CalcOutputPage";
    }

    // アクションメソッド：掛け算の実行
    @PostMapping("/multiply")
    public String multiply(@Validated CalcSession calcSession, BindingResult errors,
            Model model) {

        LOGGER.info("[ CalcController#multiply ]");

        // 入力値検証の結果を調べ、エラー発生時は入力画面へ遷移する
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }

        // ビジネスロジックを呼び出す
        BigDecimal result = null;
        try {
            result = calcService.multiply(calcSession.getParam1(),
                    calcSession.getParam2());

        } catch (LimitOverException exception) {
            return limitOver(calcSession, model, exception);
        }

        // 計算結果をcalcSessionに格納する
        calcSession.setResult(result);

        // 遷移先ページ（結果画面）の論理名を返却する
        return "CalcOutputPage";
    }

    // アクションメソッド：割り算の実行
    @PostMapping("/divide")
    public String divide(@Validated CalcSession calcSession, BindingResult errors,
            Model model) {

        LOGGER.info("[ CalcController#divide ]");

        // 入力値検証の結果を調べ、エラー発生時は入力画面へ遷移する
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }

        // ビジネスロジックを呼び出す
        BigDecimal result = null;
        try {
            result = calcService.divide(calcSession.getParam1(),
                    calcSession.getParam2());

        } catch (ZeroDivideException exception) {
            return zeroDivide(model, exception);
        }

        // 計算結果をcalcSessionに格納する
        calcSession.setResult(result);

        // 遷移先ページ（結果画面）の論理名を返却する
        return "CalcOutputPage";
    }

    // アクションメソッド：税金を計算
    @PostMapping("/calcTax")
    public String calcTax(CalcSession calcSession, Model model,
            SessionStatus sessionStatus) {

        LOGGER.info("[ CalcController#calcTax ]");

        // ビジネスロジックを呼び出す
        BigDecimal afterTaxAmount = taxService.calcTax(calcSession.getResult());

        // セッションスコープに格納されたcalcSessionに処理終了マークを付ける
        sessionStatus.setComplete();

        // 計算結果をModelに格納する
        model.addAttribute("afterTaxAmount", afterTaxAmount);

        // 遷移先ページ（結果画面）の論理名を返却する
        return "TaxOutputPage";
    }

    // 上限超過の実行
    private String limitOver(CalcSession calcSession, Model model,
            LimitOverException exception) {
        LOGGER.error("極度オーバーが発生しました", exception);
        String errorMessage = messageSource.getMessage("error.limit.over",
                new String[] {calcSession.getParam1().toString(),
                        calcSession.getParam2().toString()}, Locale.JAPANESE);
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
