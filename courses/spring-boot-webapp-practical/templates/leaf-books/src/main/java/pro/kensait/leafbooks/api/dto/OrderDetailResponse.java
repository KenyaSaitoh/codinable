package pro.kensait.leafbooks.api.dto;

import java.math.BigDecimal;

/*
 * 注文詳細に使用するデータ
 */
public record OrderDetailResponse(
        Integer orderDetailId,
        Integer bookId,
        String bookName,
        String publisherName,
        BigDecimal price,
        Integer count
) {}

