package pro.kensait.junit.cart;

/*
 * ショッピングカートを表すクラス（テスト対象）
 */
public class Cart {
    private int total; // 合計金額

    // 商品を追加する
    public void add(int price) {
        total += price; // 合計を加算する
    }

    // 商品を取り消す
    public void remove(int price) {
        if (total < price) { // 合計より大きい金額は取り消せない
            throw new IllegalStateException("cart total is " + total);
        }
        total -= price; // 合計を減算する
    }

    // カートが空か判定する
    public boolean isEmpty() {
        return total == 0;
    }

    public int getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return "Cart [total=" + total + "]";
    }
}
