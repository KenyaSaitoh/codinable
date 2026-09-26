package pro.kensait.leafbooks.service.order;

import java.math.BigDecimal;

/*
 * カート明細の機能を提供するクラス
 */
public class CartItem {
    // 書籍ID
    private Integer bookId;
    // 書籍名
    private String bookName;
    // 出版社名
    private String publisherName;
    // 価格
    private BigDecimal price;
    // 個数
    private Integer count;
    // 削除フラグ
    private boolean remove;
    // バージョン（楽観的ロック用）
    private Long version;

    // 引数のないコンストラクタ
    public CartItem() {
    }

    // すべてのフィールドをパラメータに持つコンストラクタ
    public CartItem(Integer bookId, String bookName, String publisherName,
            BigDecimal price, Integer count, boolean remove, Long version) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.publisherName = publisherName;
        this.price = price;
        this.count = count;
        this.remove = remove;
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

    // 書籍名称の取得
    public String getBookName() {
        return bookName;
    }

    // 書籍名称の設定
    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    // 出版社名称の取得
    public String getPublisherName() {
        return publisherName;
    }

    // 出版社名称の設定
    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    // 価格の取得
    public BigDecimal getPrice() {
        return price;
    }

    // 価格の設定
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    // 件数の取得
    public Integer getCount() {
        return count;
    }

    // 件数の設定
    public void setCount(Integer count) {
        this.count = count;
    }

    // removeの判定
    public boolean isRemove() {
        return remove;
    }

    // removeの設定
    public void setRemove(boolean remove) {
        this.remove = remove;
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
        return "CartItem [bookId=" + bookId + ", bookName=" + bookName
                + ", publisherName=" + publisherName + ", price=" + price + ", count="
                + count + ", remove=" + remove + ", version=" + version + "]";
    }
}

