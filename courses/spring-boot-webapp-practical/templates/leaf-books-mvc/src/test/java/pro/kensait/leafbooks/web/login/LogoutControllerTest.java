package pro.kensait.leafbooks.web.login;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/*
 * ログアウトのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LogoutControllerのテスト")
class LogoutControllerTest {
    
    private MockMvc mockMvc;
    private LogoutController logoutController;
    
    // 各テストケースで共通的な前処理
    @BeforeEach
    void setUp() {
        logoutController = new LogoutController();
        mockMvc = MockMvcBuilders.standaloneSetup(logoutController).build();
    }
    
    // 「ログアウト処理が実行されること」の検証
    @Test
    @DisplayName("ログアウト処理が実行されること")
    void test_logout() throws Exception {
        // 実行・検証フェーズ：/logoutSuccessにアクセスする
        mockMvc.perform(get("/logoutSuccess"))
                .andExpect(status().isOk())
                .andExpect(view().name("FinishPage"));
    }
}
