package pro.kensait.jakartaee.employee.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/*
 * 接続先の決め方（ClientOptions）のテスト。社員APIは使わない
 */
class ClientOptionsTest {
    // 明示したベースURLを受け付け、末尾のスラッシュを除く
    @Test
    void acceptsExplicitBaseUrlAndRemovesTrailingSlash() {
        assertEquals("http://127.0.0.1:18080/api",
                ClientOptions.baseUrl(new String[] {"http://127.0.0.1:18080/api/"}));
    }

    // 余分な引数は受け付けない
    @Test
    void rejectsExtraArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> ClientOptions.baseUrl(new String[] {"http://localhost:8080", "unexpected"}));
    }

    // HTTP 以外や、クエリ・フラグメント付きの URL は受け付けない
    @Test
    void rejectsNonHttpOrNonBaseUrls() {
        for (String url : new String[] {"", "file:///tmp/example", "http:/missing-host",
                "http://localhost:8080?x=1", "http://localhost:8080#fragment"}) {
            assertThrows(IllegalArgumentException.class,
                    () -> ClientOptions.baseUrl(new String[] {url}), url);
        }
    }
}
