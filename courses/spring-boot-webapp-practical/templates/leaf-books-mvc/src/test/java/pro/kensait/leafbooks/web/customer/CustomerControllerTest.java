package pro.kensait.leafbooks.web.customer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import pro.kensait.leafbooks.external.CustomerApiClient;
import pro.kensait.leafbooks.web.login.TokenProcessor;

/*
 * 顧客のテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerControllerのテスト")
class CustomerControllerTest {
    
    private MockMvc mockMvc;
    private CustomerController customerController;
    
    // テスト対象クラスの呼び出し先（モック）
    @Mock
    private CustomerApiClient customerApiClient;
    
    @Mock
    private TokenProcessor tokenProcessor;
    
    // 各テストケースで共通的な前処理
    @BeforeEach
    void setUp() throws Exception {
        // CustomerControllerインスタンスを作成し、モックを手動で注入する
        customerController = new CustomerController();
        
        // PasswordEncoderは実際のインスタンスを使用
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        java.lang.reflect.Field passwordEncoderField = CustomerController.class.getDeclaredField("passwordEncoder");
        passwordEncoderField.setAccessible(true);
        passwordEncoderField.set(customerController, passwordEncoder);
        
        java.lang.reflect.Field tokenProcessorField = CustomerController.class.getDeclaredField("tokenProcessor");
        tokenProcessorField.setAccessible(true);
        tokenProcessorField.set(customerController, tokenProcessor);
        
        java.lang.reflect.Field customerApiClientField = CustomerController.class.getDeclaredField("customerApiClient");
        customerApiClientField.setAccessible(true);
        customerApiClientField.set(customerController, customerApiClient);
        
        // HttpSessionはMockHttpSessionを使用
        MockHttpSession session = new MockHttpSession();
        java.lang.reflect.Field sessionField = CustomerController.class.getDeclaredField("session");
        sessionField.setAccessible(true);
        sessionField.set(customerController, session);
        
        // MockMvcをスタンドアロンモードでセットアップする
        mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
    }
    
    /*
     * ナビゲーションのテスト
     */
    @Nested
    @DisplayName("画面遷移のテスト")
    class NavigationTest {
        
        // 「顧客登録ページに遷移できること」の検証
        @Test
        @DisplayName("顧客登録ページに遷移できること")
        void test_toRegister() throws Exception {
            // 実行・検証フェーズ
            mockMvc.perform(get("/toRegister"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("CustomerInputPage"));
        }
    }
    
    /*
     * registerのテスト
     */
    @Nested
    @DisplayName("顧客登録のテスト")
    @SuppressWarnings("unused")
    class RegisterTest {
        
        // 「顧客を登録できること」の検証
        @Test
        @DisplayName("顧客を登録できること")
        void test_register() throws Exception {
            // 実行・検証フェーズ
            mockMvc.perform(post("/register")
                            .param("customerId", "1")
                            .param("password", "pass1234")
                            .param("customerName", "テスト太郎")
                            .param("email", "test@example.com")
                            .param("birthday", "1990-01-01")
                            .param("address", "東京都中央区1-1-1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("CustomerOutputPage"));
        }
    }
}
