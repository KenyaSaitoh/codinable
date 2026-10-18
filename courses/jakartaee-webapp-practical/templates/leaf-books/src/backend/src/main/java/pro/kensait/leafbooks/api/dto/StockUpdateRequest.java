package pro.kensait.leafbooks.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StockUpdateRequest {
    @NotNull(message = "バージョンは必須です")
    private Long version;

    @NotNull(message = "在庫数は必須です")
    @Min(value = 0, message = "在庫数は0以上である必要があります")
    private Integer quantity;

    public StockUpdateRequest() {
    }

    public StockUpdateRequest(Long version, Integer quantity) {
        this.version = version;
        this.quantity = quantity;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "StockUpdateRequest [version=" + version + ", quantity=" + quantity + "]";
    }
}
