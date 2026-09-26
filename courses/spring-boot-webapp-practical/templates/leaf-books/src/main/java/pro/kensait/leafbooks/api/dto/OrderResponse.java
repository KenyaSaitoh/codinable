package pro.kensait.leafbooks.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/*
 * 注文に使用するデータ
 */
public record OrderResponse(
        Integer orderTranId,
        LocalDate orderDate,
        BigDecimal totalPrice,
        BigDecimal deliveryPrice,
        String deliveryAddress,
        Integer settlementType,
        List<OrderDetailResponse> orderDetails
) {}

