package pro.kensait.shipping;

/*
 * 配送情報の永続化を担うリポジトリ
 */
public interface ShippingRepository {
    // 挿入
    void save(ShippingOrder order);
}
