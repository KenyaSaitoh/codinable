package pro.kensait.leafbooks.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/*
 * 注文詳細の機能を提供するクラス
 */
@Entity
@Table(name = "ORDER_DETAIL")
@IdClass(OrderDetailPK.class)
public class OrderDetail {
    // 注文ID
    @Id
    @Column(name = "ORDER_TRAN_ID",
            nullable = false)
    private Integer orderTranId;

    // 注文明細ID
    @Id
    @Column(name = "ORDER_DETAIL_ID",
            nullable = false)
    private Integer orderDetailId;

    // 注文
    @ManyToOne(targetEntity = OrderTran.class)
    @JoinColumn(name = "ORDER_TRAN_ID",
            referencedColumnName = "ORDER_TRAN_ID",
            insertable = false, updatable = false) // ここがポイント！JPA教材でもちゃんと説明する
    private OrderTran orderTran;

    // 書籍
    @ManyToOne(targetEntity = Book.class)
    @JoinColumn(name = "BOOK_ID",
            referencedColumnName = "BOOK_ID")
    private Book book;

    // 価格
    // 購入時点の価格を履歴に記録するため、あえて関連は使わず独立したフィールドにする
    @Column(name = "PRICE")
    private BigDecimal price;

    // 注文数
    @Column(name = "COUNT")
    private Integer count;

    // 引数なしのコンストラクタ
    public OrderDetail() {
    }

    // コンストラクタ
    public OrderDetail(Integer orderTranId, Integer orderDetailId, Book book, 
            Integer count) {
        this.orderTranId = orderTranId;
        this.orderDetailId = orderDetailId;
        this.book = book;
        this.price = book.getPrice();
        this.count = count;
    }

    // 注文明細IDへのアクセサメソッド（ゲッタのみ）
    public int getOrderDetailId() {
        return orderDetailId;
    }

    // 注文詳細IDの設定
    public void setOrderDetailId(int orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    // 注文tranの取得
    public OrderTran getOrderTran() {
        return orderTran;
    }

    // 注文tranの設定
    public void setOrderTran(OrderTran orderTran) {
        this.orderTran = orderTran;
    }

    // 書籍の取得
    public Book getBook() {
        return book;
    }

    // 書籍の設定
    public void setBook(Book book) {
        this.book = book;
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
    public int getCount() {
        return count;
    }
    
    // 件数の設定
    public void setCount(int count) {
        this.count = count;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "OrderDetail [orderTranId=" + orderTranId + ", orderDetailId="
                + orderDetailId + ", orderTran=" + orderTran + ", book=" + book
                + ", price=" + price + ", count=" + count + "]";
    }
}
