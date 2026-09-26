package pro.kensait.jpa.company.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * メールアドレスの機能を提供するクラス
 */
@Entity
@Table(name = "EMAIL")
public class Email {
    // メールID
    @Id
    @Column(name = "EMAIL_ID")
    private Integer emailId;

    // 所有者ID
    @Column(name = "HOLDER_ID")
    private Integer holderId;

    // メールアドレス
    @Column(name = "ADDRESS")
    private String address;

    // 引数なしのコンストラクタ
    public Email() {
    }

    // コンストラクタ
    public Email(Integer emailId, Integer holderId, String address) {
        this.emailId = emailId;
        this.holderId = holderId;
        this.address = address;
    }

    // アクセサメソッド
    public Integer getEmailId() {
        return emailId;
    }

    // メールアドレスIDの設定
    public void setEmailId(Integer emailId) {
        this.emailId = emailId;
    }

    // アクセサメソッド
    public Integer getHolderId() {
        return holderId;
    }

    // 保持IDの設定
    public void setHolderId(Integer holderId) {
        this.holderId = holderId;
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
        return "Email [emailId=" + emailId + ", holderId=" + holderId + ", address=" + address + "]";
    }
}
