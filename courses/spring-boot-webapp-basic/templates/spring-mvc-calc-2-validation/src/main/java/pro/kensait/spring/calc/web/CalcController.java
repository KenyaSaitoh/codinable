package pro.kensait.spring.calc.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import pro.kensait.spring.calc.service.CalcService;

/*
 * 計算機能のコントローラーを担うクラス
 */
@Controller
public class CalcController {

    // インジェクションポイント
    @Autowired
    private CalcService calcService;

    /*
     * @ModelAttributeアノテーションを付与すると、
     * リクエスト時にフォームパラメータがCalcParamインスタンスに
     * 自動的にセットされる
     * また、当該CalcParamインスタンスがModelに自動的に登録される
     */ 

    // アクションメソッド：入力画面への遷移
    @GetMapping("/")
    public String index(@ModelAttribute("calcParam") CalcParam calcParam) {
        return "CalcInputPage";
    }

    // 足し算の実行
    @PostMapping("/add")
    // BindingResultは、必ずパラメータの直後の引数に指定する
    public String add(@Validated CalcParam calcParam, BindingResult errors,
            Model model) {

        // 入力値検証の結果を調べ、エラー発生時は入力画面へ遷移する
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }

        // ビジネスロジックを呼び出す
        double result = calcService.add(calcParam.param1(), calcParam.param2());

        // 計算結果をModelに格納する
        model.addAttribute("result", result);

        // 遷移先ページ（結果画面）の論理名を返却する
        return "CalcOutputPage";
    }

    // 引き算の実行
    @PostMapping("/subtract")
    public String subtract(@Validated CalcParam calcParam, BindingResult errors,
            Model model) {

        // 入力値検証の結果を調べ、エラー発生時は入力画面へ遷移する
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }

        // ビジネスロジックを呼び出す
        double result = calcService.subtract(calcParam.param1(), calcParam.param2());

        // 計算結果をModelに格納する
        model.addAttribute("result", result);

        // 遷移先ページ（結果画面）の論理名を返却する
        return "CalcOutputPage";
    }
}