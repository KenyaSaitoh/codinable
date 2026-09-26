package pro.kensait.leafbooks.external;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/*
 * 顧客APIクライアントのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerApiClientのテスト")
class CustomerApiClientTest {
    
    // テスト対象クラス
    @InjectMocks
    private CustomerApiClient customerApiClient;
    
    // テスト対象クラスの呼び出し先
    @Mock
    private RestTemplate restTemplate;
    
    // すべてのテストケースで共通的なフィクスチャ
    private CustomerTO testCustomer;
    private String baseUrl;
    
    // 各テストケースで共通的な前処理
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:8080/api/customers";
        ReflectionTestUtils.setField(customerApiClient, "baseUrl", baseUrl);
        
        testCustomer = new CustomerTO(1, "Alice", "$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2", 
                "alice@gmail.com", LocalDate.of(1998, 4, 10), "東京都中央区1-1-1");
    }
    
    /*
     * get顧客のテスト
     */
    @Nested
    @DisplayName("顧客取得のテスト")
    class GetCustomerTest {
        
        // 「IDで顧客を取得できること」の検証
        @Test
        @DisplayName("IDで顧客を取得できること")
        void test_getById() {
            // 準備フェーズ：RestTemplateのモック動作を設定する
            String url = baseUrl + "/1";
            ResponseEntity<CustomerTO> responseEntity = ResponseEntity.ok(testCustomer);
            when(restTemplate.getForEntity(url, CustomerTO.class)).thenReturn(responseEntity);
            
            // 実行フェーズ：IDで顧客を取得する
            CustomerTO result = customerApiClient.getById(1);
            
            // 検証フェーズ：期待値と実測値が一致しているかを検証する
            assertNotNull(result);
            assertEquals(1, result.customerId());
            assertEquals("Alice", result.customerName());
            verify(restTemplate, times(1)).getForEntity(url, CustomerTO.class);
        }
        
        // 「メールアドレスで顧客を検索できること」の検証
        @Test
        @DisplayName("メールアドレスで顧客を検索できること")
        void test_queryByEmail() {
            // 準備フェーズ：RestTemplateのモック動作を設定する
            String email = "alice@gmail.com";
            ResponseEntity<CustomerTO> responseEntity = ResponseEntity.ok(testCustomer);
            when(restTemplate.getForEntity(anyString(), eq(CustomerTO.class)))
                    .thenReturn(responseEntity);
            
            // 実行フェーズ：メールアドレスで顧客を検索する
            CustomerTO result = customerApiClient.queryByEmail(email);
            
            // 検証フェーズ：期待値と実測値が一致しているかを検証する
            assertNotNull(result);
            assertEquals(email, result.email());
            verify(restTemplate, times(1)).getForEntity(anyString(), eq(CustomerTO.class));
        }
        
        // 「存在しないメールアドレスで検索するとCustomerNotFoundExceptionがスローされること」の検証
        @Test
        @DisplayName("存在しないメールアドレスで検索するとCustomerNotFoundExceptionがスローされること")
        void test_queryByEmail_NotFound() {
            // 準備フェーズ：RestTemplateのモック動作を設定する（404エラー）
            String email = "notfound@example.com";
            when(restTemplate.getForEntity(anyString(), eq(CustomerTO.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));
            
            // 実行・検証フェーズ：例外がスローされることを検証する
            assertThrows(CustomerNotFoundException.class, () -> {
                customerApiClient.queryByEmail(email);
            });
        }
        
        // 「誕生日から顧客リストを検索できること」の検証
        @Test
        @DisplayName("誕生日から顧客リストを検索できること")
        @SuppressWarnings("unchecked")
        void test_queryFromBirthday() {
            // 準備フェーズ：RestTemplateのモック動作を設定する
            LocalDate birthday = LocalDate.of(1998, 4, 10);
            List<CustomerTO> customerList = Arrays.asList(testCustomer);
            ResponseEntity<List<CustomerTO>> responseEntity = ResponseEntity.ok(customerList);
            
            when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), 
                    any(ParameterizedTypeReference.class)))
                    .thenReturn(responseEntity);
            
            // 実行フェーズ：誕生日から顧客リストを検索する
            List<CustomerTO> result = customerApiClient.queryFromBirthday(birthday);
            
            // 検証フェーズ：期待値と実測値が一致しているかを検証する
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(birthday, result.get(0).birthday());
        }
    }
    
    /*
     * modify顧客のテスト
     */
    @Nested
    @DisplayName("顧客登録・更新・削除のテスト")
    class ModifyCustomerTest {
        
        // 「顧客を新規登録できること」の検証
        @Test
        @DisplayName("顧客を新規登録できること")
        void test_create() {
            // 準備フェーズ：RestTemplateのモック動作を設定する
            ResponseEntity<CustomerTO> responseEntity = ResponseEntity.ok(testCustomer);
            when(restTemplate.postForEntity(anyString(), any(CustomerTO.class), 
                    eq(CustomerTO.class)))
                    .thenReturn(responseEntity);
            
            // 実行フェーズ：顧客を新規登録する
            CustomerTO result = customerApiClient.create(testCustomer);
            
            // 検証フェーズ：期待値と実測値が一致しているかを検証する
            assertNotNull(result);
            assertEquals(testCustomer.customerName(), result.customerName());
            verify(restTemplate, times(1)).postForEntity(anyString(), 
                    any(CustomerTO.class), eq(CustomerTO.class));
        }
        
        // 「既に存在する顧客を登録するとCustomerExistsExceptionがスローされること」の検証
        @Test
        @DisplayName("既に存在する顧客を登録するとCustomerExistsExceptionがスローされること")
        void test_create_AlreadyExists() {
            // 準備フェーズ：RestTemplateのモック動作を設定する（409エラー）
            when(restTemplate.postForEntity(anyString(), any(CustomerTO.class), 
                    eq(CustomerTO.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT));
            
            // 実行・検証フェーズ：例外がスローされることを検証する
            assertThrows(CustomerExistsException.class, () -> {
                customerApiClient.create(testCustomer);
            });
        }
        
        // 「顧客情報を置換できること」の検証
        @Test
        @DisplayName("顧客情報を置換できること")
        void test_replace() {
            // 準備フェーズ：RestTemplateのモック動作を設定する
            doNothing().when(restTemplate).put(anyString(), any(CustomerTO.class));
            
            // 実行フェーズ：顧客情報を置換する
            CustomerTO result = customerApiClient.replace(1, testCustomer);
            
            // 検証フェーズ：期待値と実測値が一致しているかを検証する
            assertNotNull(result);
            assertEquals(testCustomer, result);
            verify(restTemplate, times(1)).put(anyString(), any(CustomerTO.class));
        }
        
        // 「顧客を削除できること」の検証
        @Test
        @DisplayName("顧客を削除できること")
        void test_delete() {
            // 準備フェーズ：RestTemplateのモック動作を設定する
            doNothing().when(restTemplate).delete(anyString());
            
            // 実行・検証フェーズ：顧客を削除し、例外がスローされないことを検証する
            assertDoesNotThrow(() -> customerApiClient.delete(1));
            verify(restTemplate, times(1)).delete(anyString());
        }
    }
}

