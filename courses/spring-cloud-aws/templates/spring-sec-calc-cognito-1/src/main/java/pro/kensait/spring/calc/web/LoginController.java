package pro.kensait.spring.calc.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/*
 * ログインのコントローラーを担うクラス
 * （ログイン画面はCognitoのマネージドログインが提供するため、エラー画面への遷移のみを担当する）
 */
@Controller
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(
            LoginController.class);

    // アクションメソッド：ログインエラー画面への遷移
    @GetMapping("/loginError")
    public String loginError() {
        logger.info("[ LoginController#loginError ]");
        return "LoginErrorPage";
    }
}
