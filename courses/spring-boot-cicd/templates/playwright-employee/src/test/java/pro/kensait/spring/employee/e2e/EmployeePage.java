package pro.kensait.spring.employee.e2e;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
/** 参照元のHTMLを変えず、ラベルと社員名で画面を操作する */
public class EmployeePage {
    private final Page page;
    private final String baseUrl;

    // 社員の初期化
    public EmployeePage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    // 社員の表示
    public void open() {
        page.navigate(baseUrl + "/employees");
    }

    // 項目の実行
    public Locator field(String label) {
        return page.getByLabel(label, new Page.GetByLabelOptions().setExact(true));
    }

    // 行一覧の実行
    public Locator rows() {
        return page.locator("#employees tr:has(td)");
    }

    // registrationフォームの表示
    public void openRegistrationForm() {
        click("新規登録");
    }

    // fill社員の実行
    public void fillEmployee(String name, String departmentId, String jobId, String salary, String date) {
        field("氏名").fill(name);
        field("部署").selectOption(departmentId);
        field("役職").selectOption(jobId);
        field("月給").fill(salary);
        field("入社日").fill(date);
    }

    // clickの実行
    public void click(String buttonName) {
        page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(buttonName).setExact(true)).click();
    }

    // データの保存
    public void save() {
        click("保存");
    }

    // 社員行の実行
    public Locator employeeRow(String name) {
        return rows().filter(new Locator.FilterOptions().setHas(page.getByRole(AriaRole.CELL,
                new Page.GetByRoleOptions().setName(name).setExact(true))));
    }

    // 社員の更新
    public void edit(String name) {
        employeeRow(name).getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("編集")).click();
    }

    // APIメソッド：Employeeの削除
    public void delete(String name) {
        employeeRow(name).getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("削除")).click();
    }

    // アクションメソッド：メッセージ式のページへの遷移
    public Locator message(String message) {
        return page.getByText(message, new Page.GetByTextOptions().setExact(true));
    }

    // データの検索
    public void search(String keyword, String departmentId, String jobId, String minimum, String maximum) {
        field("キーワード").fill(keyword);
        field("部署").selectOption(departmentId);
        field("役職").selectOption(jobId);
        field("月給（下限）").fill(minimum);
        field("月給（上限）").fill(maximum);
        click("検索");
    }

    // 名称の検索
    public void searchName(String name) {
        search(name, "", "", "", "");
    }
}
