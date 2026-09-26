package pro.kensait.spring.calc;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
 * 計算機能のコントローラーを担うクラス
 */
@Controller
public class CalcController {

    // アクションメソッド：入力画面への遷移
    @GetMapping("/")
    public String index() {
        return "CalcInputPage";
    }

    // アクションメソッド：足し算実行（POSTメソッド）
    @PostMapping("/add")
    public String add(@RequestParam Double param1,
            @RequestParam Double param2, Model model) {

        // ビジネスロジックを呼び出す
        // ここではサービスに処理を委譲せず、直接実装
        double result = param1 + param2;

        // 計算結果をModelに格納する
        model.addAttribute("result", result);

        // 遷移先ページ（結果画面）の論理名を返却する
        return "CalcOutputPage";
    }

    // アクションメソッド：足し算実行（GETメソッド）
    @GetMapping("/addByGet")
    public String addByGet(@RequestParam Double param1,
            @RequestParam Double param2, Model model) {

        // ビジネスロジックを呼び出す
        // ここではサービスに処理を委譲せず、直接実装
        double result = param1 + param2;

        // 計算結果をModelに格納する
        model.addAttribute("result", result);

        // 遷移先ページ（結果画面）の論理名を返却する
        return "CalcOutputPage";
    }
}