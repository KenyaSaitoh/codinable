package pro.kensait.spring.employee.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import pro.kensait.spring.employee.Application;

/*
 * 社員playwrightのテスト
 */
@Tag("e2e")
@Execution(ExecutionMode.SAME_THREAD)
@SpringBootTest(classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// BrowserContextを分けてもDBは共有される初期社員を触らず、追加した社員は失敗時も片付ける
@Sql(statements = "DELETE FROM EMPLOYEE WHERE EMPLOYEE_ID > 10",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class EmployeePlaywrightTest {
    private static Playwright playwright;
    private static Browser browser;
    @LocalServerPort
    private int port;
    private BrowserContext context;
    private Page page;
    private EmployeePage employeePage;

    // テスト全体の前処理
    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(Boolean.parseBoolean(System.getProperty("playwright.headless", "true"))));
    }

    // テスト前処理
    @BeforeEach
    void createContext() {
        context = browser.newContext();
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true).setSnapshots(true).setSources(true));
        page = context.newPage();
        employeePage = new EmployeePage(page, "http://localhost:" + port);
    }

    // テスト後処理
    @AfterEach
    void saveArtifactsAndCloseContext(TestInfo info) throws Exception {
        if (context == null) {
            return;
        }
        try {
            Path artifacts = Path.of("build", "reports", "playwright");
            Files.createDirectories(artifacts);
            String name = info.getTestMethod().orElseThrow().getName();
            try {
                if (page != null && !page.isClosed()) {
                    page.screenshot(new Page.ScreenshotOptions()
                            .setFullPage(true).setPath(artifacts.resolve(name + ".png")));
                }
            } finally {
                // スクリーンショットの失敗時もTraceの保存を試みる
                context.tracing().stop(new Tracing.StopOptions().setPath(artifacts.resolve(name + ".zip")));
            }
        } finally {
            context.close();
        }
    }

    // テスト全体の後処理
    @AfterAll
    static void closeBrowser() {
        try {
            if (browser != null) {
                browser.close();
            }
        } finally {
            if (playwright != null) {
                playwright.close();
            }
        }
    }

    // 「社員の登録・編集・削除」の検証
    @Test
    void registersEditsAndDeletesEmployee() {
        employeePage.open();
        assertThat(employeePage.rows()).hasCount(5);
        employeePage.openRegistrationForm();
        employeePage.fillEmployee("Alice Test", "1", "1", "300000", "2026-04-01");
        employeePage.save();
        assertThat(page.locator("#paging")).hasText("1/3ページ（11件）");
        employeePage.searchName("Alice Test");
        assertThat(employeePage.employeeRow("Alice Test").locator("td").nth(1)).hasText("営業部");
        assertThat(employeePage.employeeRow("Alice Test").locator("td").nth(3)).hasText("300,000円");
        employeePage.edit("Alice Test");
        assertForm("Alice Test", "1", "1", "300000", "2026-04-01");
        String editUrl = page.url();
        employeePage.fillEmployee("Bob Test", "2", "2", "320000", "2026-05-01");
        employeePage.save();
        employeePage.searchName("Bob Test");
        assertThat(employeePage.employeeRow("Bob Test").locator("td").nth(1)).hasText("開発部");
        assertThat(employeePage.employeeRow("Bob Test").locator("td").nth(2)).hasText("主任");
        assertThat(employeePage.employeeRow("Bob Test").locator("td").nth(3)).hasText("320,000円");
        employeePage.edit("Bob Test");
        assertThat(page).hasURL(editUrl);
        assertForm("Bob Test", "2", "2", "320000", "2026-05-01");
        assertThat(page.locator("input[name=version]")).hasValue("1");
        employeePage.click("一覧へ戻る");
        employeePage.searchName("Bob Test");
        employeePage.delete("Bob Test");
        assertThat(page.locator("#paging")).hasText("1/2ページ（10件）");
        employeePage.searchName("E2E");
        assertThat(employeePage.rows()).hasCount(0);
        page.reload();
        assertThat(employeePage.message("該当する社員はいません")).isVisible();
    }

    // 「サーバー入力検証と入力値の保持」の検証
    @Test
    void showsServerValidationAndPreservesInput() {
        employeePage.open();
        employeePage.openRegistrationForm();
        employeePage.fillEmployee("Carol Test", "1", "1", "-1", "2026-04-01");
        employeePage.save();
        assertThat(employeePage.message("月給は0以上9999999以下で入力してください")).isVisible();
        assertForm("Carol Test", "1", "1", "-1", "2026-04-01");
        employeePage.click("一覧へ戻る");
        employeePage.searchName("Carol Test");
        assertThat(employeePage.rows()).hasCount(0);
    }

    // 「全必須項目エラーの表示」の検証
    @Test
    void showsAllRequiredFieldErrors() {
        employeePage.open();
        employeePage.openRegistrationForm();
        employeePage.save();
        for (String message : new String[] {"氏名を入力してください", "部署を選んでください", "役職を選んでください",
                "月給は0以上9999999以下で入力してください", "入社日を入力してください"}) {
            assertThat(employeePage.message(message)).isVisible();
        }
        employeePage.click("一覧へ戻る");
        assertThat(page.locator("#paging")).hasText("1/2ページ（10件）");
    }

    // 「編集失敗時の保存値維持と再修正」の検証
    @Test
    void failedEditKeepsStoredValuesAndCanBeCorrected() {
        employeePage.open();
        employeePage.openRegistrationForm();
        employeePage.fillEmployee("Dave Test", "1", "1", "300000", "2026-04-01");
        employeePage.save();
        employeePage.searchName("Dave Test");
        employeePage.edit("Dave Test");
        String editUrl = page.url();
        employeePage.fillEmployee("Ellen Test", "2", "2", "-1", "2026-05-01");
        employeePage.save();
        assertThat(employeePage.message("月給は0以上9999999以下で入力してください")).isVisible();
        assertForm("Ellen Test", "2", "2", "-1", "2026-05-01");
        Page storedPage = context.newPage();
        try {
            storedPage.navigate(editUrl);
            EmployeePage stored = new EmployeePage(storedPage, "http://localhost:" + port);
            assertThat(stored.field("氏名")).hasValue("Dave Test");
            assertThat(stored.field("部署")).hasValue("1");
            assertThat(stored.field("役職")).hasValue("1");
            assertThat(stored.field("月給")).hasValue("300000");
            assertThat(stored.field("入社日")).hasValue("2026-04-01");
        } finally {
            storedPage.close();
        }
        employeePage.field("月給").fill("320000");
        employeePage.save();
        employeePage.searchName("Ellen Test");
        assertThat(employeePage.rows()).hasCount(1);
        employeePage.edit("Ellen Test");
        assertThat(page).hasURL(editUrl);
        assertForm("Ellen Test", "2", "2", "320000", "2026-05-01");
    }

    // 「部署による社員の絞り込み」の検証
    @Test
    void filtersEmployeesByDepartment() {
        employeePage.open();
        employeePage.search("", "2", "", "", "");
        assertThat(employeePage.rows()).hasCount(3);
        assertThat(page.locator("#employees tr td:nth-child(1)"))
                .hasText(new String[] {"Bob", "Frank", "Ivan"});
        assertThat(employeePage.field("部署")).hasValue("2");
    }

    // 「複合条件・空結果表示・条件クリア」の検証
    @Test
    void combinesFiltersShowsEmptyResultAndClearsConditions() {
        employeePage.open();
        employeePage.search("Ivan", "2", "1", "320000", "320000");
        assertThat(employeePage.rows()).hasCount(1);
        assertThat(employeePage.employeeRow("Ivan")).isVisible();
        assertThat(employeePage.field("月給（下限）")).hasValue("320000");
        employeePage.search("Ivan", "2", "1", "320001", "400000");
        assertThat(employeePage.rows()).hasCount(0);
        assertThat(employeePage.message("該当する社員はいません")).isVisible();
        employeePage.search("", "", "", "", "");
        assertThat(employeePage.rows()).hasCount(5);
        assertThat(page.locator("#paging")).hasText("1/2ページ（10件）");
    }

    // 「検索条件を維持したページ移動」の検証
    @Test
    void movesBetweenPagesWithoutLosingSearchConditions() {
        employeePage.open();
        employeePage.search("", "", "", "250000", "700000");
        assertThat(page.locator("#prev")).isDisabled();
        employeePage.click("次へ");
        assertThat(page.locator("#paging")).hasText("2/2ページ（10件）");
        assertThat(page.locator("#employees tr td:nth-child(1)"))
                .hasText(new String[] {"Frank", "Ivan", "Justin", "Mallory", "Matilda"});
        assertThat(employeePage.field("月給（下限）")).hasValue("250000");
        assertThat(page.locator("#next")).isDisabled();
        employeePage.click("前へ");
        assertThat(employeePage.employeeRow("Alice")).isVisible();
        assertThat(page.locator("#paging")).hasText("1/2ページ（10件）");
    }

    // 「未存在社員の一覧画面メッセージ」の検証
    @Test
    void missingEmployeeShowsMessageOnList() {
        page.navigate("http://localhost:" + port + "/employees/99999/edit");
        assertThat(employeePage.message("該当する社員が見つかりません")).isVisible();
        assertThat(employeePage.rows()).hasCount(5);
        employeePage.openRegistrationForm();
        assertThat(employeePage.field("氏名")).hasValue("");
    }

    // 「ホーム画面での社員一覧表示」の検証
    @Test
    void homeDisplaysEmployeeList() {
        page.navigate("http://localhost:" + port + "/");
        assertThat(page.locator("h1")).hasText("社員管理");
        assertThat(employeePage.rows()).hasCount(5);
        assertThat(page.locator("#paging")).hasText("1/2ページ（10件）");
    }

    // assertフォームの実行
    private void assertForm(String name, String departmentId, String jobId, String salary, String date) {
        assertThat(employeePage.field("氏名")).hasValue(name);
        assertThat(employeePage.field("部署")).hasValue(departmentId);
        assertThat(employeePage.field("役職")).hasValue(jobId);
        assertThat(employeePage.field("月給")).hasValue(salary);
        assertThat(employeePage.field("入社日")).hasValue(date);
    }
}
