package pro.kensait.playwright.bookstore;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;

public class BookStoreOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
        return new Options()
                .setHeadless(false) // Webブラウザの画面を表示する 【1】
                .setContextOptions(new Browser.NewContextOptions()
                        .setBaseURL("http://localhost:8080")); // 【2】
    }
}
