package pro.kensait.leafbooks.api.dto;

public class StockTO {
    private Integer bookId;
    private Integer quantity;
    private Long version;

    public StockTO() {
    }

    public StockTO(Integer bookId, Integer quantity, Long version) {
        this.bookId = bookId;
        this.quantity = quantity;
        this.version = version;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "StockTO [bookId=" + bookId + ", quantity=" + quantity + ", version=" + version + "]";
    }
}
