package pro.kensait.leafbooks.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/*
 * カート明細に使用するデータ
 */
public record CartItemRequest(
        @NotNull(message = "書籍IDは必須です")
        Integer bookId,
        
        @NotNull(message = "書籍名は必須です")
        String bookName,
        
        @NotNull(message = "出版社名は必須です")
        String publisherName,
        
        @NotNull(message = "価格は必須です")
        BigDecimal price,
        
        @NotNull(message = "数量は必須です")
        @Min(value = 1, message = "数量は1以上である必要があります")
        Integer count
) {}

