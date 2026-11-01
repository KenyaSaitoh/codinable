package pro.kensait.junit.shipping;

/*
 * 荷物を表すレコードクラス
 */
public record Baggage (
    BaggageType baggageType,
    boolean isFragile) {
}
