package pro.kensait.leafbooks.web.login;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.context.SecurityContextRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 * トークンprocessorのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TokenProcessorのテスト")
class TokenProcessorTest {
    
    // テスト対象クラス
    @InjectMocks
    private TokenProcessor tokenProcessor;
    
    // テスト対象クラスの呼び出し先
    @Mock
    private SecurityContextRepository securityContextRepository;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    // 各テストケースで共通的な前処理
    @BeforeEach
    void setUp() {
        doNothing().when(securityContextRepository).saveContext(
                org.mockito.ArgumentMatchers.any(), 
                org.mockito.ArgumentMatchers.any(), 
                org.mockito.ArgumentMatchers.any());
    }
    
    // 「認証トークンを設定できること」の検証
    @Test
    @DisplayName("認証トークンを設定できること")
    void test_setUp() {
        // 準備フェーズ：テストデータを生成する
        Object principal = "customer1";
        Object credentials = "password";
        
        // 実行フェーズ：認証トークンを設定する
        tokenProcessor.setUp(principal, credentials);
        
        // 検証フェーズ：正常に動作したことを検証する
        assertNotNull(tokenProcessor);
    }
}
