package pro.kensait.leafbooks.api.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/*
 * 注文に使用するデータ
 */
public record OrderRequest(
        @NotEmpty(message = "カートアイテムは必須です")
        @Valid
        List<CartItemRequest> cartItems,
        
        @NotNull(message = "合計金額は必須です")
        BigDecimal totalPrice,
        
        @NotNull(message = "配送料は必須です")
        BigDecimal deliveryPrice,
        
        @NotBlank(message = "配送先住所は必須です")
        String deliveryAddress,
        
        @NotNull(message = "決済方法は必須です")
        Integer settlementType
) {}

