package pro.kensait.spring.calc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/*
 * リクエストを受け取り、CalcService に計算させ、表示するテンプレートの名前を返す。
 *
 * サーバーサイド MVC では、返り値の文字列 ("CalcInputPage") が
 * src/main/resources/templates/CalcInputPage.html に対応する。
 * HTML はサーバーで組み立てられ、完成した状態でブラウザーに届く。
 */
@Controller
public class CalcController {

    // コンストラクタで受け取ると、この Controller が何に依存しているかが型で分かる
    private final CalcService calcService;

    public CalcController(CalcService calcService) {
        this.calcService = calcService;
    }

    /*
     * @ModelAttribute を付けた引数は Model にも登録されるため、
     * テンプレート側から ${calcParam} として参照できる。
     */
    @GetMapping("/")
    public String index(@ModelAttribute("calcParam") CalcParam calcParam) {
        return "CalcInputPage";
    }

    @PostMapping("/add")
    // BindingResult は検証対象の引数の直後に置く (順番が変わると受け取れない)
    public String add(@Validated CalcParam calcParam, BindingResult errors, Model model) {
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }
        model.addAttribute("result", calcService.add(calcParam.param1(), calcParam.param2()));
        return "CalcOutputPage";
    }

    @PostMapping("/subtract")
    public String subtract(@Validated CalcParam calcParam, BindingResult errors, Model model) {
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }
        model.addAttribute("result", calcService.subtract(calcParam.param1(), calcParam.param2()));
        return "CalcOutputPage";
    }

    @PostMapping("/multiply")
    public String multiply(@Validated CalcParam calcParam, BindingResult errors, Model model) {
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }
        model.addAttribute("result", calcService.multiply(calcParam.param1(), calcParam.param2()));
        return "CalcOutputPage";
    }

    @PostMapping("/divide")
    public String divide(@Validated CalcParam calcParam, BindingResult errors, Model model) {
        if (errors.hasErrors()) {
            return "CalcInputPage";
        }
        try {
            model.addAttribute("result",
                    calcService.divide(calcParam.param1(), calcParam.param2()));
        } catch (IllegalArgumentException e) {
            // 入力形式は正しいが処理として成立しない場合 (業務エラー) は入力画面に戻す
            model.addAttribute("error", e.getMessage());
            return "CalcInputPage";
        }
        return "CalcOutputPage";
    }
}
