package pro.kensait.leafbooks.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/*
 * 注文履歴に使用するデータ
 */
public record OrderHistoryResponse(
        LocalDate orderDate,
        Integer orderTranId,
        Integer orderDetailId,
        String bookName,
        String publisherName,
        BigDecimal price,
        Integer count
) {}

