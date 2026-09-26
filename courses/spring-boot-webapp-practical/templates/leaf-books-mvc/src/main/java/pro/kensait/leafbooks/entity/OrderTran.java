package pro.kensait.leafbooks.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/*
 * 注文tranの機能を提供するクラス
 */
@Entity
@Table(name = "ORDER_TRAN")
public class OrderTran {
    // 注文ID
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "ORDER_TRAN_ID")
    private Integer orderTranId;

    // 注文日付
    @Column(name = "ORDER_DATE")
    private LocalDate orderDate;

    // 顧客
    @Column(name = "CUSTOMER_ID")
    private Integer customerId;

    // 注文明細
    @OneToMany(targetEntity = OrderDetail.class,
            mappedBy = "orderTran")
    private List<OrderDetail> orderDetails;

    // 注文金額合計
    @Column(name = "TOTAL_PRICE")
    private BigDecimal totalPrice;

    // 配送料金
    @Column(name = "DELIVERY_PRICE")
    private BigDecimal deliveryPrice;

    // 配送先住所
    @Column(name = "DELIVERY_ADDRESS")
    private String deliveryAddress;

    // 決済方法へのアクセサメソッド
    @Column(name = "SETTLEMENT_TYPE")
    private Integer settlementType;

    // 引数なしのコンストラクタ
    public OrderTran() {
    }

    // コンストラクタ
    public OrderTran(LocalDate orderDate, Integer customerId, BigDecimal totalPrice, 
            BigDecimal deliveryPrice, String deliveryAddress, Integer settlementType) {
        this.orderDate = orderDate;
        this.customerId = customerId;
        this.totalPrice = totalPrice;
        this.deliveryPrice = deliveryPrice;
        this.deliveryAddress = deliveryAddress;
        this.settlementType = settlementType;
    }

    // 注文IDへのアクセサメソッド（ゲッタのみ）
    public Integer getOrderTranId() {
        return orderTranId;
    }

    // 注文tranIDの設定
    public void setOrderTranId(Integer orderTranId) {
        this.orderTranId = orderTranId;
    }

    // 注文日付の取得
    public LocalDate getOrderDate() {
        return orderDate;
    }

    // 注文日付の設定
    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    // 顧客の取得
    public Integer getCustomer() {
        return customerId;
    }

    // 顧客の設定
    public void setCustomer(Integer customerId) {
        this.customerId = customerId;
    }

    // 注文詳細の取得
    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    // 注文詳細の設定
    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    // 合計価格の取得
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    // 合計価格の設定
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    // 配送価格の取得
    public BigDecimal getDeliveryPrice() {
        return deliveryPrice;
    }

    // 配送価格の設定
    public void setDeliveryPrice(BigDecimal deliveryPrice) {
        this.deliveryPrice = deliveryPrice;
    }

    // 配送住所の取得
    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    // 配送住所の設定
    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    // 精算型の取得
    public Integer getSettlementType() {
        return settlementType;
    }
    
    // 精算型の設定
    public void setSettlementType(Integer settlementType) {
        this.settlementType = settlementType;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "OrderTran [orderTranId=" + orderTranId + ", orderDate=" + orderDate
                + ", customerId=" + customerId + ", totalPrice=" + totalPrice
                + ", deliveryPrice=" + deliveryPrice + ", deliveryAddress="
                + deliveryAddress + ", settlementType=" + settlementType + "]";
    }
}
