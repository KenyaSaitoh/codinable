package pro.kensait.jpa.company;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import pro.kensait.jpa.company.entity.EmployeePK;

/*
 * primaryキーのテスト
 */
class PrimaryKeyTest {
    // 「Integerキャッシュ外の値比較」の検証
    @Test
    void comparesValuesOutsideTheIntegerCache() {
        var first = new EmployeePK("TECH", Integer.valueOf("10021"));
        var second = new EmployeePK("TECH", Integer.valueOf("10021"));
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, new EmployeePK("TECH", 10022));
    }
}
