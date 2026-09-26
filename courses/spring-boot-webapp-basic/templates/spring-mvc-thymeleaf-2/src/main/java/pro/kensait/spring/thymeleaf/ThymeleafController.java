package pro.kensait.spring.thymeleaf;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import pro.kensait.spring.thymeleaf.dto.Member;

/*
 * テンプレートの構造化（フラグメント・インライン・メッセージ）を確認するための
 * コントローラーを担うクラス
 */
@Controller
public class ThymeleafController {

    // サンプルデータ：会員リスト
    private static final List<Member> MEMBER_LIST = List.of(
            new Member(1, "Alice", 28),
            new Member(2, "Bob", 35),
            new Member(3, "Carol", 42));

    // アクションメソッド：目次ページへの遷移
    @GetMapping("/")
    public String index() {
        return "IndexPage";
    }

    // アクションメソッド：フラグメントのページへの遷移
    @GetMapping("/fragment")
    public String fragment(Model model) {
        model.addAttribute("memberList", MEMBER_LIST);
        return "FragmentPage";
    }

    // アクションメソッド：共通レイアウトを適用したページへの遷移
    @GetMapping("/layout")
    public String layout(Model model) {
        model.addAttribute("memberList", MEMBER_LIST);
        return "LayoutPage";
    }

    // アクションメソッド：インライン記法のページへの遷移
    @GetMapping("/inline")
    public String inline(Model model) {
        model.addAttribute("memberName", "Alice");
        model.addAttribute("htmlText", "<strong>強調されたテキスト</strong>");
        model.addAttribute("memberList", MEMBER_LIST);
        return "InlinePage";
    }

    // アクションメソッド：メッセージ式のページへの遷移
    @GetMapping("/message")
    public String message(Model model) {
        model.addAttribute("memberName", "Alice");
        model.addAttribute("itemCount", 3);
        // メッセージのキーを変数で指定する例のためのデータ
        model.addAttribute("messageKey", "label.member.name");
        return "MessagePage";
    }
}
