package pro.kensait.customer.api;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import pro.kensait.customer.entity.Customer;
import pro.kensait.customer.service.CustomerNotFoundException;
import pro.kensait.customer.service.CustomerService;
import tools.jackson.databind.ObjectMapper;

/*
 * 顧客APIのテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerApiのテスト")
class CustomerApiTest {
    
    private MockMvc mockMvc;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    // テスト対象クラスの呼び出し先（モック）
    @Mock
    private CustomerService customerService;
    
    // テスト対象クラス
    private CustomerApi customerApi;
    
    // すべてのテストケースで共通的なフィクスチャ
    private Customer testCustomer;
    
    // 各テストケースで共通的な前処理
    @BeforeEach
    void setUp() throws Exception {
        // CustomerApiインスタンスを作成し、モックを手動で注入する
        customerApi = new CustomerApi();
        java.lang.reflect.Field field = CustomerApi.class.getDeclaredField("customerService");
        field.setAccessible(true);
        field.set(customerApi, customerService);
        
        // MockMvcをスタンドアロンモードでセットアップする（例外ハンドラーを追加）
        mockMvc = MockMvcBuilders.standaloneSetup(customerApi)
                .setControllerAdvice(new CustomerExceptionHandler())
                .build();
        
        testCustomer = new Customer("Alice", "$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2", 
                "alice@gmail.com", LocalDate.of(1998, 4, 10), "東京都中央区1-1-1");
        testCustomer.setCustomerId(1);
    }
    
    /*
     * get顧客のテスト
     */
    @Nested
    @DisplayName("顧客取得のテスト")
    @SuppressWarnings("unused")
    class GetCustomerTest {
        
        // 「IDで顧客を取得できること」の検証
        @Test
        @DisplayName("IDで顧客を取得できること")
        void test_getById() throws Exception {
            // 準備フェーズ：CustomerServiceのモック動作を設定する
            when(customerService.getCustomerById(1)).thenReturn(testCustomer);
            
            // 実行・検証フェーズ：GET /customers/1を実行する
            mockMvc.perform(get("/customers/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customerId").value(1))
                    .andExpect(jsonPath("$.customerName").value("Alice"))
                    .andExpect(jsonPath("$.email").value("alice@gmail.com"));
            
            verify(customerService, times(1)).getCustomerById(1);
        }
        
        // 「存在しないIDで404エラーが返ること」の検証
        @Test
        @DisplayName("存在しないIDで404エラーが返ること")
        void test_getById_NotFound() throws Exception {
            // 準備フェーズ：CustomerServiceのモック動作を設定する（存在しないID）
            when(customerService.getCustomerById(999))
                    .thenThrow(new CustomerNotFoundException("指定されたメールアドレスは存在しません"));
            
            // 実行・検証フェーズ：GET /customers/999を実行し、404が返ることを検証する
            mockMvc.perform(get("/customers/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
        
        // 「メールアドレスで顧客を検索できること」の検証
        @Test
        @DisplayName("メールアドレスで顧客を検索できること")
        void test_queryByEmail() throws Exception {
            // 準備フェーズ：CustomerServiceのモック動作を設定する
            when(customerService.getCustomerByEmail("alice@gmail.com"))
                    .thenReturn(testCustomer);
            
            // 実行・検証フェーズ：GET /customers/query_email?email=...を実行する
            mockMvc.perform(get("/customers/query_email")
                            .param("email", "alice@gmail.com")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customerId").value(1))
                    .andExpect(jsonPath("$.customerName").value("Alice"))
                    .andExpect(jsonPath("$.email").value("alice@gmail.com"));
            
            verify(customerService, times(1)).getCustomerByEmail("alice@gmail.com");
        }
        
        // 「誕生日から顧客リストを検索できること」の検証
        @Test
        @DisplayName("誕生日から顧客リストを検索できること")
        void test_queryFromBirthday() throws Exception {
            // 準備フェーズ：CustomerServiceのモック動作を設定する
            LocalDate birthday = LocalDate.of(1990, 1, 1);
            List<Customer> customers = Arrays.asList(testCustomer);
            when(customerService.searchCustomersFromBirthday(birthday)).thenReturn(customers);
            
            // 実行・検証フェーズ：GET /customers/query_birthday?birthday=...を実行する
            mockMvc.perform(get("/customers/query_birthday")
                            .param("birthday", "1990-01-01")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].customerId").value(1))
                    .andExpect(jsonPath("$[0].customerName").value("Alice"));
            
            verify(customerService, times(1)).searchCustomersFromBirthday(birthday);
        }
    }
    
    /*
     * create顧客のテスト
     */
    @Nested
    @DisplayName("顧客登録のテスト")
    @SuppressWarnings("unused")
    class CreateCustomerTest {
        
        // 「顧客を新規登録できること」の検証
        @Test
        @DisplayName("顧客を新規登録できること")
        void test_create() throws Exception {
            // 準備フェーズ：CustomerServiceのモック動作とリクエストボディを設定する
            when(customerService.registerCustomer(any(Customer.class))).thenReturn(testCustomer);
            
            CustomerTO requestCustomer = new CustomerTO(null, "Alice", "$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2", 
                    "alice@gmail.com", LocalDate.of(1998, 4, 10), "東京都中央区1-1-1");
            String requestBody = objectMapper.writeValueAsString(requestCustomer);
            
            // 実行・検証フェーズ：POST /customers/を実行する
            mockMvc.perform(post("/customers/")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.customerId").value(1))
                    .andExpect(jsonPath("$.customerName").value("Alice"));
            
            verify(customerService, times(1)).registerCustomer(any(Customer.class));
        }
    }
    
    /*
     * replace顧客のテスト
     */
    @Nested
    @DisplayName("顧客更新のテスト")
    @SuppressWarnings("unused")
    class ReplaceCustomerTest {
        
        // 「顧客を置換できること」の検証
        @Test
        @DisplayName("顧客を置換できること")
        void test_replace() throws Exception {
            // 準備フェーズ：CustomerServiceのモック動作とリクエストボディを設定する
            doNothing().when(customerService).replaceCustomer(any(Customer.class));
            
            CustomerTO requestCustomer = new CustomerTO(null, "Alice", "$2a$10$.mUq6NV8iIo0juyT0AMB6OrWCvZZ7pG.ajIzr8KPESQ5Wa2ubLhl2", 
                    "alice@gmail.com", LocalDate.of(1998, 4, 10), "東京都中央区1-1-1");
            String requestBody = objectMapper.writeValueAsString(requestCustomer);
            
            // 実行・検証フェーズ：PUT /customers/1を実行する
            mockMvc.perform(put("/customers/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk());
            
            verify(customerService, times(1)).replaceCustomer(any(Customer.class));
        }
    }
    
    /*
     * delete顧客のテスト
     */
    @Nested
    @DisplayName("顧客削除のテスト")
    @SuppressWarnings("unused")
    class DeleteCustomerTest {
        
        // 「顧客を削除できること」の検証
        @Test
        @DisplayName("顧客を削除できること")
        void test_delete() throws Exception {
            // 準備フェーズ：CustomerServiceのモック動作を設定する
            doNothing().when(customerService).deleteCustomer(1);
            
            // 実行・検証フェーズ：DELETE /customers/1を実行する
            mockMvc.perform(delete("/customers/1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
            
            verify(customerService, times(1)).deleteCustomer(1);
        }
    }
}

