package pro.kensait.jakartaee.employee.web;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/*
 * React 画面の「Googleでログイン」が開く入口（/login。web.xml で保護している）
 * 未ログインなら認証メカニズムが Google へ転送し、ログイン後は redirectToOriginalResource でここへ戻る
 * ここでは React 画面へリダイレクトするだけ
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    @Inject
    @ConfigProperty(name = "frontend.url", defaultValue = "http://localhost:5173/")
    private String frontendUrl;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(frontendUrl);
    }
}
