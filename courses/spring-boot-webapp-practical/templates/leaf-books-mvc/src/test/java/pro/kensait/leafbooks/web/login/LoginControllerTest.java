package pro.kensait.leafbooks.web.login;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/*
 * ログインのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginControllerのテスト")
class LoginControllerTest {
    
    private MockMvc mockMvc;
    private LoginController loginController;
    
    @Mock
    private TokenProcessor tokenProcessor;
    
    // 各テストケースで共通的な前処理
    @BeforeEach
    void setUp() throws Exception {
        loginController = new LoginController();
        java.lang.reflect.Field field = LoginController.class.getDeclaredField("tokenProcessor");
        field.setAccessible(true);
        field.set(loginController, tokenProcessor);
        
        mockMvc = MockMvcBuilders.standaloneSetup(loginController).build();
    }
    
    // Note: /toLogin と /loginError エンドポイントはこのコントローラーには存在しません
    // これらはSpring Securityの設定で処理されます
}
