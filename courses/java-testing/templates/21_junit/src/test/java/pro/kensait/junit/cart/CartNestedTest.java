package pro.kensait.junit.cart;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/*
 * Cart（ショッピングカート）のためのテストクラス
 * ネステッドクラスによって階層化
 */
public class CartNestedTest {
    Cart cart;

    @Nested //【1】
    @DisplayName("空のカートに対するテスト")
    class EmptyCartTest {
        @BeforeEach
        void setUp() {
            cart = new Cart(); //【2】
        }

        @Test
        @DisplayName("商品の追加のテスト")
        void test_Add_EmptyCart() {
            cart.add(3000);
            assertEquals(3000, cart.getTotal());
        }

        @Test
        @DisplayName("商品の取り消しのテスト")
        void test_Remove_EmptyCart() {
            assertThrows(IllegalStateException.class, () -> {
                cart.remove(3000);
            });
        }

        @Test
        @DisplayName("空かどうかの判定のテスト")
        void test_IsEmpty_EmptyCart() {
            assertTrue(cart.isEmpty());
        }
    }

    @Nested
    @DisplayName("合計10000円のカートに対するテスト")
    class NonEmptyCartTest {
        @BeforeEach
        void setUp() {
            cart = new Cart();
            cart.add(10000);
        }

        @Test
        @DisplayName("商品の追加のテスト")
        void test_Add_NonEmptyCart() {
            cart.add(3000);
            assertEquals(13000, cart.getTotal());
        }

        @Test
        @DisplayName("商品の取り消しのテスト")
        void test_Remove_NonEmptyCart() {
            cart.remove(3000);
            assertEquals(7000, cart.getTotal());
        }

        @Test
        @DisplayName("空かどうかの判定のテスト")
        void test_IsEmpty_NonEmptyCart() {
            assertFalse(cart.isEmpty());
        }
    }
}
