package pro.kensait.shipping;

import java.time.Instant;

/*
 * 配送注文を表すレコード
 */
public record ShippingOrder(String postalCode, int weightGrams,
        int shippingCost, Instant orderedAt) {
}
