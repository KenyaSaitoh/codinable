package pro.kensait.spring.thymeleaf;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import pro.kensait.spring.thymeleaf.dto.Member;

/*
 * Thymeleafの基本構文を確認するためのコントローラーを担うクラス
 * ここでの主役はテンプレート側であり、コントローラーはModelにデータを詰めるだけ
 */
@Controller
public class ThymeleafController {

    // サンプルデータ：会員リスト
    private static final List<Member> MEMBER_LIST = List.of(
            new Member(1, "Alice", 28, "alice@example.com", true),
            new Member(2, "Bob", 35, "bob@example.com", false),
            new Member(3, "Carol", 42, "carol@example.com", false));

    // サンプルデータ：国と首都のマップ（出力順を固定するためLinkedHashMapを使う）
    private static final Map<String, String> CITY_MAP = new LinkedHashMap<>();

    static {
        CITY_MAP.put("Japan", "Tokyo");
        CITY_MAP.put("France", "Paris");
        CITY_MAP.put("Egypt", "Cairo");
    }

    // アクションメソッド：目次ページへの遷移
    @GetMapping("/")
    public String index() {
        return "IndexPage";
    }

    // アクションメソッド：式構文のページへの遷移
    @GetMapping("/expression")
    public String expression(Model model) {
        model.addAttribute("memberName", "Alice");
        model.addAttribute("price", 1200);
        model.addAttribute("admin", true);
        // 値がnullの場合の挙動（エルビス演算子）を確認するためのデータ
        model.addAttribute("nickname", null);
        // エスケープの有無を確認するためのデータ
        model.addAttribute("htmlText", "<strong>強調されたテキスト</strong>");
        return "ExpressionPage";
    }

    // アクションメソッド：オブジェクトとコレクションのページへの遷移
    @GetMapping("/collection")
    public String collection(Model model) {
        model.addAttribute("member", MEMBER_LIST.get(0));
        model.addAttribute("memberList", MEMBER_LIST);
        model.addAttribute("cityMap", CITY_MAP);
        return "CollectionPage";
    }

    // アクションメソッド：条件分岐のページへの遷移
    @GetMapping("/condition")
    public String condition(Model model) {
        model.addAttribute("member", MEMBER_LIST.get(0));
        model.addAttribute("memberList", MEMBER_LIST);
        model.addAttribute("editable", true);
        model.addAttribute("created", false);
        model.addAttribute("orderStatus", "SHIPPED");
        return "ConditionPage";
    }

    // アクションメソッド：リンクURL式と属性設定のページへの遷移
    @GetMapping("/link")
    public String link(Model model) {
        model.addAttribute("memberList", MEMBER_LIST);
        // URLエンコードされることを確認するため、空白を含む文字列を渡す
        model.addAttribute("keyword", "spring boot");
        model.addAttribute("submittable", false);
        return "LinkPage";
    }

    // アクションメソッド：会員詳細ページに遷移する（リンクURL式の遷移先）
    @GetMapping("/members/{memberId}")
    public String memberDetail(@PathVariable("memberId") Integer memberId,
            Model model) {
        Member member = MEMBER_LIST.stream()
                .filter(m -> m.memberId().equals(memberId))
                .findFirst()
                .orElse(null);
        model.addAttribute("member", member);
        return "MemberDetailPage";
    }

    // アクションメソッド：ユーティリティオブジェクトのページへの遷移
    @GetMapping("/utility")
    public String utility(Model model) {
        model.addAttribute("bigNumber", 1000000);
        model.addAttribute("smallNumber", 0.0005);
        model.addAttribute("rawText", "  Thymeleaf  ");
        model.addAttribute("now", LocalDateTime.now());
        model.addAttribute("memberList", MEMBER_LIST);
        model.addAttribute("emptyList", List.of());
        return "UtilityPage";
    }
}
