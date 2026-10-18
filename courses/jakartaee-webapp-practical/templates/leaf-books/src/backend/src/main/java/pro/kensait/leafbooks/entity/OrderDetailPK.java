package pro.kensait.leafbooks.entity;

import java.io.Serializable;
import java.util.Objects;

// 注文明細の複合主キークラス
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

    // アクセサメソッド
    public Integer getOrderTranId() {
        return orderTranId;
    }

    public Integer getOrderDetailId() {
        return orderDetailId;
    }

    // 一意性を保証するために、必ずequalsメソッドをオーバーライドする
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OrderDetailPK other)) {
            return false;
        }
        return Objects.equals(orderTranId, other.orderTranId)
                && Objects.equals(orderDetailId, other.orderDetailId);
    }

    // equalsメソッドに合わせて、hashcodeメソッドもオーバーライドする
    @Override
    public int hashCode() {
        return Objects.hash(orderTranId, orderDetailId);
    }
}