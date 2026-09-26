package pro.kensait.leafbooks.web.cart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.DataBinder;

/*
 * カートstateのテスト
 */
class CartStateTest {
    // 明細の実行
    private CartItem item(int id, int price) {
        return new CartItem(id, "Book", "Publisher", BigDecimal.valueOf(price), 1, false, 0L);
    }

    // 「複数明細削除後のカートと合計更新」の検証
    @Test
    void removingMultipleItemsUpdatesStoredCartAndTotal() {
        var cart = new CartSession(List.of(item(1, 100), item(2, 200), item(3, 300)),
                BigDecimal.valueOf(600), BigDecimal.ZERO, "東京都", 1);
        assertEquals("CartViewPage", new CartController().removeBook(List.of(1, 2), cart));
        assertEquals(List.of(3), cart.getCartItems().stream().map(CartItem::getBookId).toList());
        assertEquals(BigDecimal.valueOf(300), cart.getTotalPrice());
    }

    // 「リスト参照を介した保存済みカートの変更防止」の検証
    @Test
    void callerCannotClearStoredCartThroughAListReference() {
        var items = new ArrayList<>(List.of(item(1, 100)));
        var cart = new CartSession(items, BigDecimal.valueOf(100), BigDecimal.ZERO, "東京都", 1);
        items.clear();
        cart.getCartItems().clear();
        assertEquals(1, cart.getCartItems().size());
        cart.setCartItems(items);
        items.add(item(2, 200));
        assertEquals(0, cart.getCartItems().size());
    }

    // 「インデックス付きフォームによるカート明細更新」の検証
    @Test
    void indexedFormBindingStillUpdatesCartItemFields() {
        var cart = new CartSession(List.of(item(1, 100)), BigDecimal.valueOf(100),
                BigDecimal.ZERO, "東京都", 1);
        var binder = new DataBinder(cart);
        var values = new MutablePropertyValues();
        values.add("cartItems[0].count", "2");
        binder.bind(values);
        assertFalse(binder.getBindingResult().hasErrors());
        assertEquals(2, cart.getCartItems().get(0).getCount());
    }
}
