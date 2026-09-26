package pro.kensait.spring.employee.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/*
 * クライアントオプションのテスト
 */
class ClientOptionsTest {
    // 「明示したベースURLの受付と末尾スラッシュの除去」の検証
    @Test
    void acceptsExplicitBaseUrlAndRemovesTrailingSlash() {
        assertEquals("http://127.0.0.1:18080", ClientOptions.baseUrl(
                new String[] {"http://127.0.0.1:18080/"}));
    }

    // 「余分な引数の拒否」の検証
    @Test
    void rejectsExtraArguments() {
        assertThrows(IllegalArgumentException.class, () -> ClientOptions.baseUrl(
                new String[] {"http://localhost:8080", "unexpected"}));
    }

    // 「HTTP以外または不正なベースURLの拒否」の検証
    @Test
    void rejectsNonHttpOrNonBaseUrls() {
        for (String url : new String[] {"", "file:///tmp/example", "http:/missing-host",
                "http://localhost:8080?x=1", "http://localhost:8080#fragment"}) {
            assertThrows(IllegalArgumentException.class,
                    () -> ClientOptions.baseUrl(new String[] {url}), url);
        }
    }
}
