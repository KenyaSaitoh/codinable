package pro.kensait.leafbooks.entity;

import java.io.Serializable;
import java.util.Objects;

/*
 * 注文詳細主キーの機能を提供するクラス
 */
public class OrderDetailPK implements Serializable {
    // 注文ID
    private Integer orderTranId;

    // 注文明細ID
    private Integer orderDetailId;

    // 引数なしのコンストラクタ
    public OrderDetailPK() {
    }

    // コンストラクタ
    public OrderDetailPK(int orderTranId, int orderDetailId) {
        this.orderTranId = orderTranId;
        this.orderDetailId = orderDetailId;
    }

    // 注文IDへのアクセサメソッド（ゲッタのみ）
    public Integer getOrderTranId() {
        return orderTranId;
    }

    // 注文明細IDへのアクセサメソッド（ゲッタのみ）
    public Integer getOrderDetailId() {
        return orderDetailId;
    }

    // 一意性を保証するために、必ずequalsメソッドをオーバーライド
    public boolean equals(Object obj) {
        return ((obj instanceof OrderDetailPK) &&
                Objects.equals(orderTranId, ((OrderDetailPK)obj).getOrderTranId()) &&
                Objects.equals(orderDetailId, ((OrderDetailPK)obj).getOrderDetailId()));
    }

    // equalsメソッドに合わせて、hashcodeメソッドもオーバーライド
    public int hashCode() {
        return Objects.hash(orderTranId, orderDetailId);
    }
}
