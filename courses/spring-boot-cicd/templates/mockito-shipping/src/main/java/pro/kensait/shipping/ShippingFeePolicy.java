package pro.kensait.shipping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
/** 旧講座の配送料割引を、円単位・四捨五入という明示的な仕様で表す */
public class ShippingFeePolicy {
    // 割引の適用
    public int applyDiscount(Membership membership, int baseCost) {
        Objects.requireNonNull(membership, "会員区分は必須です");
        if (baseCost < 0) {
            throw new IllegalArgumentException("配送料は0円以上で指定してください");
        }
        return switch (membership) {
            case REGULAR -> baseCost;
            case GOLD -> discounted(baseCost, "0.90", 3000);
            case DIAMOND -> discounted(baseCost, "0.75", 2500);
        };
    }

    // 割引後の実行
    private int discounted(int baseCost, String rate, int floor) {
        if (baseCost <= floor) {
            return baseCost;
        }
        int discountedCost = BigDecimal.valueOf(baseCost)
                .multiply(new BigDecimal(rate))
                .setScale(0, RoundingMode.HALF_UP).intValueExact();
        return Math.max(discountedCost, floor);
    }
}
