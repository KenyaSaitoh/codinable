package pro.kensait.jpa.company.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/*
 * 住所の機能を提供するクラス
 */
@Embeddable
public class Address {
    // 郵便番号
    @Column(name = "ZIP_CODE")
    private String zipCode;
    // 市町村
    @Column(name = "CITY")
    private String city;
    // 番地
    @Column(name = "STREET")
    private String street;

    // 引数なしのコンストラクタ
    public Address() {
    }

    // コンストラクタ
    public Address(String zipCode, String city, String street) {
        this.zipCode = zipCode;
        this.city = city;
        this.street = street;
    }

    // アクセサメソッド
    public String getZipCode() {
        return zipCode;
    }

    // ZIPコードの設定
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    // 市区町村の取得
    public String getCity() {
        return city;
    }

    // 市区町村の設定
    public void setCity(String city) {
        this.city = city;
    }

    // streetの取得
    public String getStreet() {
        return street;
    }

    // streetの設定
    public void setStreet(String street) {
        this.street = street;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Address [zipCode=" + zipCode + ", city=" + city + ", street=" + street + "]";
    }
}
