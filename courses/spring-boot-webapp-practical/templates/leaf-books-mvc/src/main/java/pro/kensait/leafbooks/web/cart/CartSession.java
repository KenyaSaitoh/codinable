package pro.kensait.leafbooks.web.cart;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/*
 * カートの機能を提供するクラス
 */
public class CartSession {
    // カートアイテムのリスト
    private List<CartItem> cartItems;
    // 注文金額合計
    private BigDecimal totalPrice;
    // 配送料金
    private BigDecimal deliveryPrice;
    // 配送先住所
    @NotEmpty
    @Size(min = 1, max = 40)
    private String deliveryAddress;
    // 決済方法
    @NotNull
    private Integer settlementType;

    // 引数の無いコンストラクタ
    public CartSession() {
    }

    // 全フィールドを引数にとるコンストラクタ
    public CartSession(List<CartItem> cartItems, BigDecimal totalPrice,
            BigDecimal deliveryPrice, String deliveryAddress, Integer settlementType) {
        this.cartItems = cartItems == null ? null : new CopyOnWriteArrayList<>(cartItems);
        this.totalPrice = totalPrice;
        this.deliveryPrice = deliveryPrice;
        this.deliveryAddress = deliveryAddress;
        this.settlementType = settlementType;
    }

    // アクセサメソッド
    public List<CartItem> getCartItems() {
        return cartItems == null ? null : new CopyOnWriteArrayList<>(cartItems);
    }

    // カート明細の設定
    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems == null ? null : new CopyOnWriteArrayList<>(cartItems);
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
        return "CartSession [cartItems=" + cartItems + ", totalPrice=" + totalPrice
                + ", deliveryPrice=" + deliveryPrice + ", deliveryAddress="
                + deliveryAddress + ", settlementType=" + settlementType + "]";
    }
}
