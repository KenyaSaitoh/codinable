package pro.kensait.jpa.company.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * 住所の機能を提供するクラス
 */
@Entity
@Table(name = "ADDRESS")
public class Address {
    // 社員ID
    @Id
    @Column(name = "ADDRESS_ID")
    private Integer addressId;

    // 郵便番号
    @Column(name = "ZIP_CODE")
    private String zipCode;

    // 都道府県
    @Column(name = "PREFECTURE")
    private String prefecture;

    // 市町村
    @Column(name = "CITY") 
    private String city;

    // 引数なしのコンストラクタ
    public Address() {
    }

    // コンストラクタ
    public Address(Integer addressId, String zipCode, String prefecture, 
            String city) {
        this.addressId = addressId;
        this.zipCode = zipCode;
        this.prefecture = prefecture;
        this.city = city;
    }

    // アクセサメソッド
    public Integer getAddressId() {
        return addressId;
    }

    // 住所IDの設定
    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    // アクセサメソッド
    public String getZipCode() {
        return zipCode;
    }

    // ZIPコードの設定
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    // 都道府県の取得
    public String getPrefecture() {
        return prefecture;
    }

    // 都道府県の設定
    public void setPrefecture(String prefecture) {
        this.prefecture = prefecture;
    }

    // 市区町村の取得
    public String getCity() {
        return city;
    }

    // 市区町村の設定
    public void setCity(String city) {
        this.city = city;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Address [addressId=" + addressId + ", zipCode=" + zipCode + ", prefecture=" + prefecture + ", city="
                + city + "]";
    }
}
