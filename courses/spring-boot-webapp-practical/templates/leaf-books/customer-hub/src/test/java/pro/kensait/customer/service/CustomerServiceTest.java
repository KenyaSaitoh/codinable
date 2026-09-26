package pro.kensait.customer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import pro.kensait.customer.entity.Customer;
import pro.kensait.customer.repository.CustomerRepository;

/*
 * 顧客のテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerServiceのテスト")
class CustomerServiceTest {
    
    // テスト対象クラス
    @InjectMocks
    private CustomerService customerService;
    
    // テスト対象クラスの呼び出し先
    @Mock
    private CustomerRepository customerRepository;
    
    // すべてのテストケースで共通的なフィクスチャ
    private Customer testCustomer;
    
    // 各テストケースで共通的な前処理
    @BeforeEach
    void setUp() {
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
        void test_getCustomerById() {
            // 準備フェーズ：CustomerRepositoryのモック動作を設定する
            when(customerRepository.findById(1)).thenReturn(Optional.of(testCustomer));
            
            // 実行フェーズ：IDで顧客を取得する
            Customer result = customerService.getCustomerById(1);
            
            // 検証フェーズ：期待値と実測値が一致しているかを検証する
            assertNotNull(result);
            assertEquals("Alice", result.getCustomerName());
            assertEquals("alice@gmail.com", result.getEmail());
            verify(customerRepository, times(1)).findById(1);
        }
        
        // 「存在しないIDで例外がスローされること」の検証
        @Test
        @DisplayName("存在しないIDで例外がスローされること")
        void test_getCustomerById_NotFound() {
            // 準備フェーズ：CustomerRepositoryのモック動作を設定する（存在しないID）
            when(customerRepository.findById(999)).thenReturn(Optional.empty());
            
            // 実行・検証フェーズ：顧客取得を試み、例外がスローされることを検証する
            CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, 
                    () -> customerService.getCustomerById(999));
            assertNotNull(exception);
        }
        
        // 「メールアドレスで顧客を取得できること」の検証
        @Test
        @DisplayName("メールアドレスで顧客を取得できること")
        void test_getCustomerByEmail() {
            // 準備フェーズ：CustomerRepositoryのモック動作を設定する
            when(customerRepository.findCustomerByEmail("alice@gmail.com"))
                    .thenReturn(Optional.of(testCustomer));
            
            // 実行フェーズ：メールアドレスで顧客を取得する
            Customer result = customerService.getCustomerByEmail("alice@gmail.com");
            
            // 検証フェーズ：期待値と実測値が一致しているかを検証する
            assertNotNull(result);
            assertEquals("Alice", result.getCustomerName());
            verify(customerRepository, times(1)).findCustomerByEmail("alice@gmail.com");
        }
        
        // 「存在しないメールアドレスで例外がスローされること」の検証
        @Test
        @DisplayName("存在しないメールアドレスで例外がスローされること")
        void test_getCustomerByEmail_NotFound() {
            // 準備フェーズ：CustomerRepositoryのモック動作を設定する（存在しないメール）
            when(customerRepository.findCustomerByEmail("notfound@example.com"))
                    .thenReturn(Optional.empty());
            
            // 実行・検証フェーズ：顧客取得を試み、例外がスローされることを検証する
            CustomerNotFoundException exception = assertThrows(CustomerNotFoundException.class, 
                    () -> customerService.getCustomerByEmail("notfound@example.com"));
            assertNotNull(exception);
        }
    }
    
    /*
     * 検索顧客のテスト
     */
    @Nested
    @DisplayName("顧客検索のテスト")
    @SuppressWarnings("unused")
    class SearchCustomerTest {
        
        // 「誕生日から顧客リストを検索できること」の検証
        @Test
        @DisplayName("誕生日から顧客リストを検索できること")
        void test_searchCustomersFromBirthday() {
            // 準備フェーズ：CustomerRepositoryのモック動作を設定する
            LocalDate from = LocalDate.of(1990, 1, 1);
            List<Customer> customers = Arrays.asList(testCustomer);
            when(customerRepository.searchCustomersFromBirthday(from)).thenReturn(customers);
            
            // 実行フェーズ：誕生日から顧客リストを検索する
            List<Customer> result = customerService.searchCustomersFromBirthday(from);
            
            // 検証フェーズ：期待値と実測値が一致しているかを検証する
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(customerRepository, times(1)).searchCustomersFromBirthday(from);
        }
    }
    
    /*
     * register顧客のテスト
     */
    @Nested
    @DisplayName("顧客登録のテスト")
    @SuppressWarnings("unused")
    class RegisterCustomerTest {
        
        // 「顧客を新規登録できること」の検証
        @Test
        @DisplayName("顧客を新規登録できること")
        void test_registerCustomer() {
            // 準備フェーズ：CustomerRepositoryのモック動作を設定する
            when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
            
            // 実行フェーズ：顧客を新規登録する
            Customer result = customerService.registerCustomer(testCustomer);
            
            // 検証フェーズ：期待値と実測値が一致しているかを検証する
            assertNotNull(result);
            assertEquals("Alice", result.getCustomerName());
            verify(customerRepository, times(1)).save(testCustomer);
        }
        
        // 「メールアドレス重複時に例外がスローされること」の検証
        @Test
        @DisplayName("メールアドレス重複時に例外がスローされること")
        void test_registerCustomer_DuplicateEmail() {
            // 準備フェーズ：CustomerRepositoryのモック動作を設定する（一意制約違反）
            when(customerRepository.save(any(Customer.class)))
                    .thenThrow(new DataIntegrityViolationException("Duplicate email"));
            
            // 実行・検証フェーズ：顧客登録を試み、例外がスローされることを検証する
            CustomerExistsException exception = assertThrows(CustomerExistsException.class, 
                    () -> customerService.registerCustomer(testCustomer));
            assertNotNull(exception);
        }
    }
    
    /*
     * replace顧客のテスト
     */
    @Nested
    @DisplayName("顧客更新のテスト")
    @SuppressWarnings("unused")
    class ReplaceCustomerTest {
        
        // 「顧客を上書き登録できること」の検証
        @Test
        @DisplayName("顧客を上書き登録できること")
        void test_replaceCustomer() {
            // 準備フェーズ：CustomerRepositoryのモック動作を設定する
            when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);
            
            // 実行フェーズ：顧客を上書き登録する
            customerService.replaceCustomer(testCustomer);
            
            // 検証フェーズ：正しく呼び出されたことを検証する
            verify(customerRepository, times(1)).save(testCustomer);
        }
        
        // 「一意制約違反時に削除してから再登録されること」の検証
        @Test
        @DisplayName("一意制約違反時に削除してから再登録されること")
        void test_replaceCustomer_WithConstraintViolation() {
            // 準備フェーズ：CustomerRepositoryのモック動作を設定する
            when(customerRepository.save(any(Customer.class)))
                    .thenThrow(new DataIntegrityViolationException("Duplicate"))
                    .thenReturn(testCustomer);
            
            // 実行フェーズ：顧客を上書き登録する
            customerService.replaceCustomer(testCustomer);
            
            // 検証フェーズ：削除と保存が呼び出されたことを検証する
            verify(customerRepository, times(2)).save(testCustomer);
            verify(customerRepository, times(1)).delete(testCustomer);
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
        void test_deleteCustomer() {
            // 準備フェーズ：特になし（voidメソッド）
            
            // 実行フェーズ：顧客を削除する
            customerService.deleteCustomer(1);
            
            // 検証フェーズ：正しく呼び出されたことを検証する
            verify(customerRepository, times(1)).deleteById(1);
        }
    }
}

