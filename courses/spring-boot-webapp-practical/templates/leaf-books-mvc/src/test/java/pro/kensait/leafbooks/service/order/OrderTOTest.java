package pro.kensait.leafbooks.service.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import pro.kensait.leafbooks.web.cart.CartItem;

/*
 * 注文変換先のテスト
 */
class OrderTOTest {
    // 「カート消去後の注文済み明細の不変性」の検証
    @Test
    void clearingTheCartDoesNotChangeSubmittedOrderItems() {
        var items = new ArrayList<CartItem>();
        items.add(new CartItem(1, "Book", "Publisher", BigDecimal.TEN, 1, false, 0L));
        var order = new OrderTO(1, LocalDate.of(2026, 9, 21), items,
                BigDecimal.TEN, BigDecimal.ZERO, "東京都", 1);
        items.clear();
        assertEquals(1, order.cartItems().size());
        assertThrows(UnsupportedOperationException.class, () -> order.cartItems().clear());
    }
}
