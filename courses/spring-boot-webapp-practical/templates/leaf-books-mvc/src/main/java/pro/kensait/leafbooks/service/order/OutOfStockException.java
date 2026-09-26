package pro.kensait.leafbooks.service.order;

/*
 * outの在庫を通知する例外
 */
public class OutOfStockException extends RuntimeException {
    private Integer bookId;
    private String bookName;

    // outの在庫の初期化
    public OutOfStockException() {
        super();
    }

    // outの在庫の初期化
    public OutOfStockException(String message, Throwable cause) {
        super(message, cause);
    }

    // outの在庫の初期化
    public OutOfStockException(String message) {
        super(message);
    }

    // outの在庫の初期化
    public OutOfStockException(Throwable cause) {
        super(cause);
    }

    // outの在庫の初期化
    public OutOfStockException(Integer bookId, String bookName, String message) {
        super(message);
        this.bookId = bookId;
        this.bookName = bookName;
    }

    // アクセサメソッド
    public Integer getBookId() {
        return bookId;
    }

    // 書籍名称の取得
    public String getBookName() {
        return bookName;
    }
}
