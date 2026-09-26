package pro.kensait.leafbooks.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.Table;

/*
 * 書籍の機能を提供するクラス
 */
@Entity
@Table(name = "BOOK")
@SecondaryTable(name = "STOCK",
        pkJoinColumns = @PrimaryKeyJoinColumn(name = "BOOK_ID"))
public class Book {
    // 書籍ID
    @Id
    @Column(name = "BOOK_ID")
    private Integer bookId;

    // 書籍名
    @Column(name = "BOOK_NAME")
    private String bookName;

    // 著者
    @Column(name = "AUTHOR")
    private String author;

    // カテゴリ
    @ManyToOne(targetEntity = Category.class)
    @JoinColumn(name = "CATEGORY_ID",
            referencedColumnName = "CATEGORY_ID")
    private Category category;

    // 出版社
    @ManyToOne(targetEntity = Publisher.class)
    @JoinColumn(name = "PUBLISHER_ID",
            referencedColumnName = "PUBLISHER_ID")
    private Publisher publisher;

    // 価格
    @Column(name = "PRICE")
    private BigDecimal price;

    // 在庫数
    @Column(table = "STOCK", name = "QUANTITY")
    private Integer quantity;

    // 引数なしのコンストラクタ
    public Book() {
    }

    // コンストラクタ
    public Book(Integer bookId, String bookName, String author, Category category, 
            Publisher publisher, BigDecimal price, Integer quantity) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.author = author;
        this.category = category;
        this.publisher = publisher;
        this.price = price;
        this.quantity = quantity;
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

    // 著者の取得
    public String getAuthor() {
        return author;
    }

    // 著者の設定
    public void setAuthor(String author) {
        this.author = author;
    }

    // カテゴリの取得
    public Category getCategory() {
        return category;
    }

    // カテゴリの設定
    public void setCategory(Category category) {
        this.category = category;
    }

    // 出版社の取得
    public Publisher getPublisher() {
        return publisher;
    }

    // 出版社の設定
    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    // 価格の取得
    public BigDecimal getPrice() {
        return price;
    }

    // 価格の設定
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    // 数量の取得
    public Integer getQuantity() {
        return quantity;
    }

    // 数量の設定
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Book [bookId=" + bookId + ", bookName=" + bookName + ", author=" + author
                + ", category=" + category + ", publisher=" + publisher + ", price="
                + price + ", quantity=" + quantity + "]";
    }
}
