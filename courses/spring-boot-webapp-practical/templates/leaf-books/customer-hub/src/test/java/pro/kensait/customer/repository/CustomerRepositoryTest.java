package pro.kensait.customer.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import pro.kensait.customer.entity.Customer;

/*
 * 顧客のテスト
 */
@SpringBootTest
@Transactional
@Sql(scripts = "/test-data.sql")
@DisplayName("CustomerRepositoryのテスト")
class CustomerRepositoryTest {
    
    @Autowired
    private CustomerRepository customerRepository;
    
    // 「IDで顧客を検索できること」の検証
    @Test
    @DisplayName("IDで顧客を検索できること")
    void test_findById() {
        // 実行フェーズ：IDで顧客を検索する
        Optional<Customer> found = customerRepository.findById(1);
        
        // 検証フェーズ：期待値と実測値が一致しているかを検証する
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getCustomerName());
        assertEquals("alice@gmail.com", found.get().getEmail());
    }
    
    // 「メールアドレスで顧客を検索できること」の検証
    @Test
    @DisplayName("メールアドレスで顧客を検索できること")
    void test_findCustomerByEmail() {
        // 実行フェーズ：メールアドレスで顧客を検索する
        Optional<Customer> found = customerRepository.findCustomerByEmail("alice@gmail.com");
        
        // 検証フェーズ：期待値と実測値が一致しているかを検証する
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getCustomerId());
        assertEquals("Alice", found.get().getCustomerName());
    }
    
    // 「存在しないメールアドレスで検索すると空のOptionalが返ること」の検証
    @Test
    @DisplayName("存在しないメールアドレスで検索すると空のOptionalが返ること")
    void test_findCustomerByEmail_NotFound() {
        // 実行フェーズ：存在しないメールアドレスで顧客を検索する
        Optional<Customer> found = customerRepository.findCustomerByEmail("notfound@example.com");
        
        // 検証フェーズ：空のOptionalが返ることを検証する
        assertTrue(found.isEmpty());
    }
    
    // 「誕生日から顧客リストを検索できること」の検証
    @Test
    @DisplayName("誕生日から顧客リストを検索できること")
    void test_searchCustomersFromBirthday() {
        // 実行フェーズ：誕生日から顧客リストを検索する（1990年1月1日以降）
        LocalDate from = LocalDate.of(1990, 1, 1);
        List<Customer> customers = customerRepository.searchCustomersFromBirthday(from);
        
        // 検証フェーズ：期待値と実測値が一致しているかを検証する
        assertNotNull(customers);
        assertTrue(customers.size() >= 4);
        assertTrue(customers.stream()
                .allMatch(c -> !c.getBirthday().isBefore(from)));
    }
    
    // 「全顧客を取得できること」の検証
    @Test
    @DisplayName("全顧客を取得できること")
    void test_findAll() {
        // 実行フェーズ：全顧客を取得する
        List<Customer> customers = customerRepository.findAll();
        
        // 検証フェーズ：期待値と実測値が一致しているかを検証する
        assertNotNull(customers);
        assertEquals(5, customers.size());
    }
}
