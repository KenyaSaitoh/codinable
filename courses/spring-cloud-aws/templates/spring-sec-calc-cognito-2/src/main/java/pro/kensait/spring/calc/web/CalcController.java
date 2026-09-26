package pro.kensait.spring.calc.web;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    private static final Logger logger = LoggerFactory.getLogger(
            CalcController.class);

    // インジェクションポイント
    @Autowired
    private CalcService calcService;

    // インジェクションポイント
    @Autowired
    private MessageSource messageSource;

    // アクションメソッド："/"は入力ページへリダイレクト
    @GetMapping("/")
    public String index() {
        return "redirect:/toInput";
    }

    // アクションメソッド：ログイン処理を行い、電卓の入力画面への遷移
    @GetMapping("/toInput")
    public String toInput(@ModelAttribute("calcParam") CalcParam calcParam) {
        logger.info("[ CalcController#toInput ]");
        return "CalcInputPage";
    }

    // アクションメソッド：足し算の実行
    @PostMapping("/add")
    // BindingResultはパラメータの直後の引数に指定する
    public String add(@Validated CalcParam calcParam, BindingResult errors,
            @AuthenticationPrincipal OAuth2User principal, // OIDCユーザー
            RedirectAttributes attributes) {
        logger.info("[ CalcController#add ]");

        // 入力値検証の結果を調べ、エラー発生時は入力画面へ遷移する
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }

        // OIDCユーザーの"username"属性を取得する
        String username = principal.getAttribute("username").toString();

        // ビジネスロジックを呼び出す
        int id = calcService.add(calcParam.param1(), calcParam.param2(),
                username); 

        // 計算結果IDをRedirectAttributesに格納する（引き回し用）
        attributes.addAttribute("id", id);

        // 遷移先ページの論理名を返却する
        // 先頭に"redirect:"と記載することで、当該ページにリダイレクトさせることができる
        return "redirect:/viewResult";
    }

    // アクションメソッド：引き算の実行
    @PostMapping("/subtract")
    public String subtract(@Validated CalcParam calcParam, BindingResult errors,
            @AuthenticationPrincipal OAuth2User principal,
            RedirectAttributes attributes) {
        logger.info("[ CalcController#subtract ]");

        // 入力値検証の結果を調べ、エラー発生時は入力画面へ遷移する
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }

        // OIDCユーザーの"username"属性を取得する
        String username = principal.getAttribute("username").toString();

        // ビジネスロジックを呼び出す
        int id = calcService.subtract(calcParam.param1(), calcParam.param2(),
                username);

        // 計算結果IDをRedirectAttributesに格納する（引き回し用）
        attributes.addAttribute("id", id);

        // 遷移先ページの論理名を返却する
        // 先頭に"redirect:"と記載することで、当該ページにリダイレクトさせることができる
        return "redirect:/viewResult";
    }

    // アクションメソッド：掛け算の実行
    @PostMapping("/multiply")
    public String multiply(@Validated CalcParam calcParam, BindingResult errors,
            Model model, 
            @AuthenticationPrincipal OAuth2User principal, // OIDCユーザー
            RedirectAttributes attributes) {
        logger.info("[ CalcController#multiply ]");

        // 入力値検証の結果を調べ、エラー発生時は入力画面へ遷移する
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }

        // OIDCユーザーの"username"属性を取得する
        String username = principal.getAttribute("username").toString();

        // ビジネスロジックを呼び出す
        int id = 0;
        try {
            id = calcService.multiply(calcParam.param1(), calcParam.param2(),
                    username);

        // ビジネスロジックでエラー（極度オーバー）が発生
        } catch(LimitOverException loe) {
            // エラーログを出力する
            logger.error("極度オーバーが発生しました", loe);

            // メッセージソースよりエラーメッセージを取得し、Modelに格納する
            String errorMessage = messageSource.getMessage("error.limit.over",
                    new String[] {calcParam.param1().toString(),
                            calcParam.param2().toString()},
                    Locale.JAPANESE);
            model.addAttribute("errorMessage", errorMessage);

            // 遷移先ページ（エラー画面）の論理名を返却する
            return "CalcErrorPage";
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
            Model model,
            @AuthenticationPrincipal OAuth2User principal, // OIDCユーザー
            RedirectAttributes attributes) {

        logger.info("[ CalcController#divide ]");

        // 入力値検証の結果を調べ、エラー発生時は入力画面へ遷移する
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }

        // OIDCユーザーの"username"属性を取得する
        String username = principal.getAttribute("username").toString();

        // ビジネスロジックを呼び出す
        int id = 0;
        try {
            id = calcService.divide(calcParam.param1(), calcParam.param2(),
                    username);

        // ビジネスロジックでエラー（ゼロ割り）が発生
        } catch(ZeroDivideException zde) {
            // エラーログを出力する
            logger.error("ゼロ割が発生しました", zde);

            // メッセージソースよりエラーメッセージを取得し、Modelに格納する
            String errorMessage = messageSource.getMessage("error.zero.divide", null,
                    Locale.JAPANESE);
            model.addAttribute("errorMessage", errorMessage);

            // 遷移先ページ（エラー画面）の論理名を返却する
            return "CalcErrorPage";
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
        logger.info("[ CalcController#viewResult ] ID => " + id);

        // ビジネスロジックを呼び出し、計算結果を取得する
        CalcResult calcResult = calcService.getCalcResult(id);

        // 計算結果をModelに格納する
        model.addAttribute("calcResult", calcResult);

        // 遷移先ページの論理名を返却する
        return "CalcOutputPage";
    }

    // アクションメソッド：履歴の表示
    @GetMapping("/viewHistory")
    public String viewList(Model model) {
        logger.info("[ CalcController#viewHistory ]");

        // ビジネスロジックを呼び出し、計算結果を取得する
        List<CalcResult> calcResults = calcService.getCalcResults();

        // 計算結果をModelに格納する
        model.addAttribute("calcResults", calcResults);

        // 遷移先ページの論理名を返却する
        return "CalcHistoryPage";
    }
}