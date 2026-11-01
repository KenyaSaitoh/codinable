package pro.kensait.playwright.bookstore;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.junit.UsePlaywright;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;

@UsePlaywright(BookStoreOptions.class)
public class BookStoreTest {

    // トレースの記録を始める
    @BeforeEach
    void startTrace(BrowserContext context) {
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true).setSnapshots(true).setSources(true));
    }

    // トレースの記録を止め、ZIP ファイルに保存する（テストが失敗しても実行される）
    @AfterEach
    void stopTrace(BrowserContext context) {
        context.tracing().stop(new Tracing.StopOptions()
                .setPath(Paths.get("trace/BookStoreTest.zip")));
    }

    @Test
    void test_BookStoreOperations(Page page) { // 【1】
        page.navigate("/"); // 1. トップページを開く
        assertThat(page).hasTitle("TopPage"); // 2.
        page.locator("#email").fill("alice@example.com"); // 3.
        page.locator("#password").fill("password"); // 4.
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("ログイン")).click(); // 5. 【2】
        assertThat(page).hasURL(Pattern.compile(".*/toSelect")); // 6. 【3】
        assertThat(page).hasTitle("BookSelectPage"); // 7.
        Locator rows = page.locator("#book-table tbody tr");
        assertThat(rows.first().locator("td").first())
                .containsText("Java SEディープダイブ"); // 8.
        assertThat(rows).hasCount(34); // 9. 【4】
        page.locator("#button-3").click(); // 10.
        assertThat(page).hasTitle("CartViewPage"); // 11.
        page.locator("#toSelectLink").click(); // 12.
        assertThat(page).hasTitle("BookSelectPage"); // 13.
        page.locator("#button-5").click(); // 14.
        assertThat(page).hasTitle("CartViewPage"); // 15.
        page.locator("#toSelectLink").click(); // 16.
        assertThat(page).hasTitle("BookSelectPage"); // 17.
        page.locator("#toSearchLink").click(); // 18.
        assertThat(page).hasTitle("BookSearchPage"); // 19.
        page.locator("#category").selectOption(new SelectOption().setLabel("SpringBoot")); // 20.
        page.locator("#keyword").fill("Cloud"); // 21.
        page.locator("#search1Button").click(); // 22.
        assertThat(page).hasTitle("BookSelectPage"); // 23.
        page.locator("#button-11").click(); // 24.
        assertThat(page).hasTitle("CartViewPage"); // 25.
        page.locator("#check-3").click(); // 26.
        page.locator("#removeButton").click(); // 27.
        assertThat(page).hasTitle("CartViewPage"); // 28.
        page.locator("#fixButton").click(); // 29.
        assertThat(page).hasTitle("BookOrderPage"); // 30.
        page.locator("input[name='settlementType'][value='1']").check(); // 31.
        page.onceDialog(dialog -> dialog.dismiss()); // 32〜33. 【1】
        page.locator("#orderButton1").click();
        assertThat(page).hasTitle("BookOrderPage"); // 【2】
        page.onceDialog(dialog -> dialog.accept()); // 34〜35.
        page.locator("#orderButton1").click();
        assertThat(page).hasTitle("OrderSuccessPage"); // 36.
        page.locator("#logoutButton").click(); // 37.
        assertThat(page).hasTitle("FinishPage"); // 38.
    }
}
