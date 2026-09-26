package pro.kensait.shipping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

/*
 * 配送料金ポリシーのテスト
 */
class ShippingFeePolicyTest {
    private final ShippingFeePolicy policy = new ShippingFeePolicy();

    // 「割引適用と最低料金」の検証
    @ParameterizedTest(name = "{0}: 基本料{1}円 → {2}円")
    @CsvSource({
            "REGULAR, 0, 0", "REGULAR, 4000, 4000",
            "GOLD, 2999, 2999", "GOLD, 3000, 3000", "GOLD, 3001, 3000",
            "GOLD, 3333, 3000", "GOLD, 3334, 3001", "GOLD, 4000, 3600",
            "DIAMOND, 2499, 2499", "DIAMOND, 2500, 2500", "DIAMOND, 2501, 2500",
            "DIAMOND, 3333, 2500", "DIAMOND, 3334, 2501", "DIAMOND, 4000, 3000"
    })
    void appliesDiscountAndFloor(Membership membership, int baseCost, int expected) {
        assertThat(policy.applyDiscount(membership, baseCost)).isEqualTo(expected);
    }

    // roundingcasesの実行
    static Stream<Arguments> roundingCases() {
        return Stream.of(Arguments.of(Membership.GOLD, 4005, 3605),
                Arguments.of(Membership.DIAMOND, 4002, 3002));
    }

    // 「四捨五入による料金計算」の検証
    @ParameterizedTest
    @MethodSource("roundingCases")
    void roundsHalfUp(Membership membership, int baseCost, int expected) {
        assertThat(policy.applyDiscount(membership, baseCost)).isEqualTo(expected);
    }

    // 「負の料金の拒否」の検証
    @Test
    void rejectsNegativeCost() {
        assertThrows(IllegalArgumentException.class,
                () -> policy.applyDiscount(Membership.REGULAR, -1));
    }

    // 「会員区分の必須制約」の検証
    @Test
    void requiresMembership() {
        assertThrows(NullPointerException.class, () -> policy.applyDiscount(null, 4000));
    }
}
