package pro.kensait.spring.hello;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/*
 * hello機能のコントローラー
 */
@Controller
public class HelloController {

    // fooの実行
    @GetMapping("/hello")
    public String foo(Model model) {
        model.addAttribute("message", "Hello, Alice!");
        return "HelloPage";
    }
}
