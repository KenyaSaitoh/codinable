package pro.kensait.leafbooks.service.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// CartItemは同じパッケージ内のため、import不要

public record OrderTO (
        // 顧客ID
        Integer customerId,
        // 注文日
        LocalDate orderDate,
        // カートアイテムのリスト
        List<CartItem> cartItems,
        // 注文金額合計
        BigDecimal totalPrice,
        // 配送料金
        BigDecimal deliveryPrice,
        // 配送先住所
        String deliveryAddress,
        // 決済方法
        Integer settlementType) {
    // 注文変換先の初期化
    public OrderTO {
        cartItems = cartItems == null ? null : List.copyOf(cartItems);
    }
}