package pro.kensait.spring.thymeleaf;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import pro.kensait.spring.thymeleaf.dto.MemberForm;

/*
 * 会員登録フォームのコントローラーを担うクラス
 */
@Controller
public class FormController {

    // 選択肢（表示ラベル => 送信値） 出力順を固定するためLinkedHashMapを使う
    private static final Map<String, String> GENDER_MAP = new LinkedHashMap<>();
    private static final Map<String, String> PREFECTURE_MAP = new LinkedHashMap<>();
    private static final Map<String, String> HOBBY_MAP = new LinkedHashMap<>();

    static {
        GENDER_MAP.put("男性", "MALE");
        GENDER_MAP.put("女性", "FEMALE");
        GENDER_MAP.put("回答しない", "UNKNOWN");

        PREFECTURE_MAP.put("北海道", "HOKKAIDO");
        PREFECTURE_MAP.put("東京都", "TOKYO");
        PREFECTURE_MAP.put("大阪府", "OSAKA");
        PREFECTURE_MAP.put("福岡県", "FUKUOKA");

        HOBBY_MAP.put("読書", "READING");
        HOBBY_MAP.put("旅行", "TRAVEL");
        HOBBY_MAP.put("料理", "COOKING");
    }

    // 選択肢は入力画面と再表示の両方で必要になるため、@ModelAttributeで常に用意
    @ModelAttribute("genderMap")
    public Map<String, String> genderMap() {
        return new LinkedHashMap<>(GENDER_MAP);
    }

    // 都道府県mapの実行
    @ModelAttribute("prefectureMap")
    public Map<String, String> prefectureMap() {
        return new LinkedHashMap<>(PREFECTURE_MAP);
    }

    // hobbymapの実行
    @ModelAttribute("hobbyMap")
    public Map<String, String> hobbyMap() {
        return new LinkedHashMap<>(HOBBY_MAP);
    }

    // アクションメソッド：入力画面への遷移
    @GetMapping("/form")
    public String form(Model model) {
        // th:object でバインドする空のフォームオブジェクトを用意する
        model.addAttribute("memberForm", new MemberForm());
        return "FormPage";
    }

    // アクションメソッド：入力内容を検証し、結果画面への遷移
    @PostMapping("/form")
    public String register(
            @ModelAttribute("memberForm") @Validated MemberForm memberForm,
            BindingResult errors) {

        // 入力値検証でエラーがあった場合は、入力画面に戻す
        // このとき、memberFormとBindingResultがModelに残っているため、
        // 入力値の保持とエラーメッセージの表示が行われる
        if (errors.hasErrors()) {
            return "FormPage";
        }

        return "FormResultPage";
    }
}
