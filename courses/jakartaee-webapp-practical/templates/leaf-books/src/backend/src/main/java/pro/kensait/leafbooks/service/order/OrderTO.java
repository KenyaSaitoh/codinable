package pro.kensait.leafbooks.service.order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// 注文を保持するDTOクラス（Recordとして定義）
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
    // 呼び出し側のリストへの追加・削除から切り離すため、要素をコピーして保持する
    public OrderTO {
        cartItems = List.copyOf(cartItems);
    }
}