package pro.kensait.spring.employee;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;
/** 負荷投入前に、実HTTPと共通CSVで対象アプリの初期状態を確認する */
@Tag("it")
@SpringBootTest(classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmployeeApplicationSmokeTest {
    @LocalServerPort private int port;

    // クライアントの実行
    private RestClient client() {
        return RestClient.create("http://localhost:" + port);
    }

    // 社員namesの実行
    private List<String> employeeNames(String html) {
        return Pattern.compile("<tr>\\s*<td>([^<]+)</td>").matcher(html).results()
                .map(match -> match.group(1)).toList();
    }

    // 「参照社員の先頭ページ表示」の検証
    @Test
    void rendersFirstPageOfReferenceEmployees() {
        String html = client().get().uri("/employees").retrieve().body(String.class);
        assertThat(html).contains("1/2ページ（10件）");
        assertThat(employeeNames(html)).containsExactly("Alice", "Bob", "Carol", "Dave", "Ellen");
    }

    // 「ヘルスチェックAPIの公開」の検証
    @Test
    void exposesHealthEndpoint() {
        String health = client().get().uri("/actuator/health").retrieve().body(String.class);
        assertThat(health).contains("\"status\":\"UP\"");
    }

    // 「フィーダーと編集フォーム・部署検索の整合性」の検証
    @ParameterizedTest
    @CsvFileSource(resources = "/employees.csv", numLinesToSkip = 1)
    void feederMatchesEditFormAndDepartmentSearch(int id, String name, int departmentId,
            int jobId, int salary, String date, String departmentNames) {
        String form = client().get().uri("/employees/{id}/edit", id).retrieve().body(String.class);
        assertThat(form).contains("action=\"/employees/" + id + "\"",
                "value=\"" + name + "\"", "value=\"" + salary + "\"", "value=\"" + date + "\"");
        assertThat(form).containsPattern("(?s)id=\"formDepartmentId\".*?value=\"" + departmentId
                + "\"[^>]*selected");
        assertThat(form).containsPattern("(?s)id=\"formJobId\".*?value=\"" + jobId + "\"[^>]*selected");
        String list = client().get().uri("/employees?departmentId={id}", departmentId)
                .retrieve().body(String.class);
        assertThat(employeeNames(list)).containsExactly(departmentNames.split("\\|"));
    }
}
