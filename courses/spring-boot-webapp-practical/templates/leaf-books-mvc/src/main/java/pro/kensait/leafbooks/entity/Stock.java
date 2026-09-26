package pro.kensait.leafbooks.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/*
 * 在庫の機能を提供するクラス
 */
@Entity
@Table(name = "STOCK")
public class Stock {
    // 書籍ID
    @Id
    @Column(name = "BOOK_ID")
    private Integer bookId;

    // 在庫数
    @Column(name = "QUANTITY")
    private Integer quantity;

    // バージョン（楽観的ロック用）
    @Version
    @Column(name = "VERSION")
    private Long version;

    //  引数なしのコンストラクタ
    public Stock() {
    }

    // コンストラクタ
    public Stock(Integer bookId, Integer quantity, Long version) {
        this.bookId = bookId;
        this.quantity = quantity;
        this.version = version;
    }

    // アクセサメソッド
    public Integer getBookId() {
        return bookId;
    }

    // 書籍IDの設定
    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    // 数量の取得
    public Integer getQuantity() {
        return quantity;
    }

    // 数量の設定
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    // バージョン（楽観的ロックで使用）へのアクセサメソッド
    public Long getVersion() {
        return version;
    }

    // バージョンの設定
    public void setVersion(Long version) {
        this.version = version;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Stock [bookId=" + bookId + ", quantity=" + quantity + ", version="
                + version + "]";
    }
}
