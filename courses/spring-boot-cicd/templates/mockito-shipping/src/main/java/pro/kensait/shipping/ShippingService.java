package pro.kensait.shipping;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
/** 外部料金取得・保存・現在時刻をコンストラクターで渡す単体テスト教材 */
public class ShippingService {
    private final ShippingRateClient rateClient;
    private final ShippingRepository repository;
    private final Clock clock;
    private final ShippingFeePolicy feePolicy = new ShippingFeePolicy();

    // 配送の初期化
    public ShippingService(ShippingRateClient rateClient, ShippingRepository repository,
            Clock clock) {
        this.rateClient = rateClient;
        this.repository = repository;
        this.clock = clock;
    }

    // 配送注文の受付
    public ShippingOrder order(String postalCode, int weightGrams, Membership membership) {
        Objects.requireNonNull(membership, "会員区分は必須です");
        if (postalCode == null || !postalCode.matches("[0-9]{7}") || weightGrams <= 0) {
            throw new IllegalArgumentException("郵便番号は7桁、重量は1g以上で指定してください");
        }
        int baseCost = rateClient.quote(postalCode, weightGrams);
        int cost = feePolicy.applyDiscount(membership, baseCost);
        ShippingOrder order = new ShippingOrder(postalCode, weightGrams, cost,
                Instant.now(clock));
        repository.save(order);
        return order;
    }
}
