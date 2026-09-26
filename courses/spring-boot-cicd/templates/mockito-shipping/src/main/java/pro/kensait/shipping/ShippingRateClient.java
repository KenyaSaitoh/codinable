package pro.kensait.shipping;

/*
 * 配送料金を呼び出すクライアント
 */
public interface ShippingRateClient {
    // quoteの実行
    int quote(String postalCode, int weightGrams);
}
