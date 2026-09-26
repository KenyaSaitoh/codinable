package pro.kensait.customer.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * 顧客の機能を提供するクラス
 */
@Entity
@Table(name = "CUSTOMER")
public class Customer {
    // 顧客ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CUSTOMER_ID")
    private Integer customerId;

    // 顧客名
    @Column(name = "CUSTOMER_NAME")
    private String customerName;

    // パスワード
    @Column(name = "PASSWORD")
    private String password;

    // メールアドレス
    @Column(name = "EMAIL")
    private String email;

    // 生年月日
    @Column(name = "BIRTHDAY")
    private LocalDate birthday;

    // 住所
    @Column(name = "ADDRESS")
    private String address;

    // 引数なしのコンストラクタ
    public Customer() {
    }

    // コンストラクタ
    public Customer(String customerName, String password, String email,
            LocalDate birthday, String address) {
        this.customerName = customerName;
        this.password = password;
        this.email = email;
        this.birthday = birthday;
        this.address = address;
    }

    // 顧客IDの取得
    public Integer getCustomerId() {
        return customerId;
    }

    // 顧客IDの設定
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    // 顧客名称の取得
    public String getCustomerName() {
        return customerName;
    }

    // 顧客名称の設定
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    // パスワードの取得
    public String getPassword() {
        return password;
    }

    // パスワードの設定
    public void setPassword(String password) {
        this.password = password;
    }

    // メールアドレスの取得
    public String getEmail() {
        return email;
    }

    // メールアドレスの設定
    public void setEmail(String email) {
        this.email = email;
    }

    // 生年月日の取得
    public LocalDate getBirthday() {
        return birthday;
    }

    // 生年月日の設定
    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    // 住所の取得
    public String getAddress() {
        return address;
    }

    // 住所の設定
    public void setAddress(String address) {
        this.address = address;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Customer [customerId=" + customerId + ", customerName=" + customerName
                + ", password=" + password + ", email=" + email + ", birthday=" + birthday
                + ", address=" + address + "]";
    }
}

