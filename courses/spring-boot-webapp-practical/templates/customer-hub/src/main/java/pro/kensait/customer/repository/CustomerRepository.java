package pro.kensait.customer.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pro.kensait.customer.entity.Customer;

/*
 * 顧客情報の永続化を担うリポジトリ
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    // 顧客メールアドレスの検索
    @Query("SELECT c FROM Customer c WHERE c.email = :email")
    Optional<Customer> findCustomerByEmail(@Param("email") String email);

    // サービスメソッド：顧客リストを取得する（誕生日からの条件検索）
    @Query("SELECT c FROM Customer c WHERE :from <= c.birthday")
    List<Customer> searchCustomersFromBirthday(@Param("from") LocalDate from);
}

