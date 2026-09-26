package pro.kensait.spring.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
/** 生成されたHTTP APIを、実DB込みで確認するControllerのモックは使わない */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:hsqldb:mem:data_rest_employee_tests")
@ActiveProfiles("demo")
@Sql(scripts = {"/reset-employees.sql", "/db/3_EMPLOYEE_DML.sql"})
@Timeout(30)
class EmployeeDataRestTest {
    private static final JsonMapper JSON = JsonMapper.builder().build();
    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    private HttpClient client;

    // テスト前処理
    @BeforeEach
    void connect() {
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    }

    // 「3種類のエンティティとリポジトリリンク」の検証
    @Test
    void continuesTheThreeEntityDomainAndDiscoversRepositoryLinks() throws Exception {
        assertEquals(Set.of("Employee", "Department", "Job"),
                entityManagerFactory.getMetamodel().getEntities().stream()
                        .map(entity -> entity.getName()).collect(Collectors.toSet()));
        JsonNode root = get("/api");
        for (String name : List.of("employees", "departments", "jobs", "profile")) {
            assertTrue(root.path("_links").has(name), name);
        }
        JsonNode employee = get("/api/employees/1");
        assertEquals("E0001", employee.path("employeeCode").asText());
        assertEquals(1, employee.path("departmentId").asInt());
        assertEquals(3, employee.path("jobId").asInt());
        assertEquals("2012-04-01", employee.path("entranceDate").asText());
        assertFalse(employee.has("departmentName"));
    }

    // 「HALナビゲーションによるページングと並び替え」の検証
    @Test
    void pagesAndSortsViaHalNavigation() throws Exception {
        JsonNode first = get("/api/employees?sort=employeeId,asc");
        assertEquals(5, first.path("page").path("size").asInt());
        assertEquals(0, first.path("page").path("number").asInt());
        assertEquals(10, first.path("page").path("totalElements").asInt());
        assertEquals(List.of(1, 2, 3, 4, 5), ids(first));
        JsonNode second = get(first.path("_links").path("next").path("href").asText());
        assertEquals(List.of(6, 7, 8, 9, 10), ids(second));
        assertTrue(second.path("_links").has("prev"));
        assertFalse(second.path("_links").has("next"));
        assertEquals(List.of(9, 2), ids(get("/api/employees?size=2&sort=salary,desc")));
        assertEquals(100, get("/api/employees?size=1000").path("page").path("size").asInt());
    }

    // 「4部署・5役職の公開」の検証
    @Test
    void exposesTheSameFourDepartmentsAndFiveJobs() throws Exception {
        JsonNode departments = get("/api/departments?sort=departmentId,asc").path("_embedded")
                .path("departments");
        JsonNode jobs = get("/api/jobs?sort=grade,asc").path("_embedded").path("jobs");
        assertEquals(4, departments.size());
        assertEquals(5, jobs.size());
        assertEquals("営業部", departments.get(0).path("departmentName").asText());
        assertEquals("東京", departments.get(0).path("location").asText());
        assertEquals("本部長", jobs.get(4).path("jobName").asText());
        assertEquals(5, jobs.get(4).path("grade").asInt());
    }

    // 「マスターデータの読み取り専用制御」の検証
    @ParameterizedTest
    @ValueSource(strings = {"departments", "jobs"})
    void keepsMasterDataReadOnly(String collection) throws Exception {
        assertEquals(405, request("POST", "/api/" + collection, Map.of(), null).statusCode());
        for (String method : List.of("PUT", "PATCH", "DELETE")) {
            assertEquals(405, request(method, "/api/" + collection + "/1", Map.of(), null).statusCode());
        }
        assertEquals(200, request("GET", "/api/" + collection + "/1", null, null).statusCode());
    }

    // 「クエリメソッドのページング対応検索リソース化」の検証
    @Test
    void turnsQueryMethodsIntoPagedSearchResources() throws Exception {
        JsonNode links = get("/api/employees/search").path("_links");
        for (String name : List.of("by-salary", "by-department", "by-job", "by-name")) {
            assertTrue(links.has(name), name);
        }
        assertFalse(links.has("findByStatus"));
        assertEquals(List.of(1, 2, 8, 9), ids(get(
                "/api/employees/search/by-salary?lowerSalary=500000&sort=employeeId,asc")));
        assertEquals(List.of(2, 6, 7), ids(get(
                "/api/employees/search/by-department?departmentId=2&sort=employeeId,asc")));
        assertEquals(List.of(3, 5, 7, 10), ids(get(
                "/api/employees/search/by-job?jobId=1&sort=employeeId,asc")));
        assertEquals(List.of(1), ids(get("/api/employees/search/by-name?keyword="
                + URLEncoder.encode("山田", StandardCharsets.UTF_8))));
        assertEquals(404, request("GET",
                "/api/employees/search/findByStatus?status=active", null, null).statusCode());
    }

    // 「既定リソースを変えない選択項目の射影」の検証
    @Test
    void projectsSelectedFieldsWithoutChangingTheDefaultResource() throws Exception {
        JsonNode summary = get("/api/employees?projection=summary&sort=employeeId,asc")
                .path("_embedded").path("employees").get(0);
        assertEquals("E0001", summary.path("employeeCode").asText());
        assertEquals(3, summary.path("jobId").asInt());
        assertFalse(summary.has("salary"));
        assertFalse(summary.has("entranceDate"));
        assertTrue(summary.path("_links").has("self"));
        assertEquals(520000, get("/api/employees/1").path("salary").asInt());
        assertEquals(200, request("GET", "/api/profile/employees", null, null).statusCode());
    }

    // 「サーバー管理項目による登録と氏名の正規化」の検証
    @Test
    void createsWithServerOwnedFieldsAndNormalizesName() throws Exception {
        Map<String, Object> input = employeeInput();
        input.put("employeeName", "  新入 社員  ");
        input.put("employeeId", 999);
        input.put("employeeCode", "FORGED");
        input.put("status", "deleted");
        input.put("version", 99);
        HttpResponse<String> response = request("POST", "/api/employees", input, null);
        assertEquals(201, response.statusCode(), response.body());
        JsonNode created = JSON.readTree(response.body());
        assertEquals(11, created.path("employeeId").asInt());
        assertEquals("E0011", created.path("employeeCode").asText());
        assertEquals("新入 社員", created.path("employeeName").asText());
        assertEquals("active", created.path("status").asText());
        String location = response.headers().firstValue("Location").orElseThrow();
        assertTrue(location.endsWith("/api/employees/11"), location);
        HttpResponse<String> loaded = request("GET", location, null, null);
        assertEquals(created, JSON.readTree(loaded.body()));
        assertEquals(etag(response), etag(loaded));
        assertEquals("E0011", jdbc.queryForObject(
                "SELECT EMPLOYEE_CODE FROM EMPLOYEE WHERE EMPLOYEE_ID = 11", String.class));
    }

    // 「ETagを用いた置換・部分更新と古い更新の拒否」の検証
    @Test
    void replacesAndPatchesWithEtagsAndRejectsStaleWrites() throws Exception {
        String originalTag = etag(request("GET", "/api/employees/1", null, null));
        Map<String, Object> input = employeeInput();
        input.put("employeeCode", "FORGED");
        input.put("status", "deleted");
        HttpResponse<String> replaced = request("PUT", "/api/employees/1", input, originalTag);
        assertEquals(200, replaced.statusCode(), replaced.body());
        assertEquals("E0001", JSON.readTree(replaced.body()).path("employeeCode").asText());
        HttpResponse<String> patched = request("PATCH", "/api/employees/1",
                Map.of("salary", 410000, "employeeName", "  更新 社員  "), etag(replaced));
        assertEquals(200, patched.statusCode(), patched.body());
        JsonNode actual = get("/api/employees/1");
        assertEquals(410000, actual.path("salary").asInt());
        assertEquals("更新 社員", actual.path("employeeName").asText());
        assertEquals("active", actual.path("status").asText());
        assertEquals(2, actual.path("departmentId").asInt());
        assertEquals(412, request("PATCH", "/api/employees/1",
                Map.of("salary", 1), originalTag).statusCode());
        assertEquals(412, request("DELETE", "/api/employees/1", null, originalTag).statusCode());
    }

    // 「条件付きGETへの対応」の検証
    @Test
    void supportsConditionalGet() throws Exception {
        String tag = etag(request("GET", "/api/employees/1", null, null));
        HttpRequest query = HttpRequest.newBuilder(uri("/api/employees/1"))
                .header("If-None-Match", tag).GET().build();
        assertEquals(304, client.send(query, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    // 「論理削除した行の全リポジトリ参照からの除外」の検証
    @Test
    void logicallyDeletesAndHidesRowsFromEveryRepositoryRead() throws Exception {
        String tag = etag(request("GET", "/api/employees/1", null, null));
        assertEquals(204, request("DELETE", "/api/employees/1", null, tag).statusCode());
        assertEquals(404, request("GET", "/api/employees/1", null, null).statusCode());
        assertEquals("deleted", jdbc.queryForObject(
                "SELECT STATUS FROM EMPLOYEE WHERE EMPLOYEE_ID = 1", String.class));
        assertEquals(1, jdbc.queryForObject(
                "SELECT VERSION FROM EMPLOYEE WHERE EMPLOYEE_ID = 1", Integer.class));
        assertEquals(9, get("/api/employees").path("page").path("totalElements").asInt());
        assertEquals(List.of(2, 8, 9), ids(get(
                "/api/employees/search/by-salary?lowerSalary=500000&sort=employeeId,asc")));
        assertEquals(List.of(5, 9), ids(get(
                "/api/employees/search/by-department?departmentId=1&sort=employeeId,asc")));
    }

    // 「登録時の必須項目検証」の検証
    @ParameterizedTest
    @ValueSource(strings = {"employeeName", "departmentId", "jobId", "salary", "entranceDate"})
    void validatesRequiredFieldsOnCreate(String field) throws Exception {
        Map<String, Object> input = employeeInput();
        input.remove(field);
        HttpResponse<String> result = request("POST", "/api/employees", input, null);
        assertEquals(400, result.statusCode(), result.body());
        assertTrue(result.body().contains(field), result.body());
        assertEquals(10, jdbc.queryForObject("SELECT COUNT(*) FROM EMPLOYEE", Integer.class));
    }

    // 「部分更新時の入力検証と不正値の未保存」の検証
    @Test
    void validatesPatchWithoutSavingInvalidChanges() throws Exception {
        for (Map<String, Object> input : List.<Map<String, Object>>of(
                Map.of("employeeName", "   "), Map.of("employeeName", "a".repeat(31)),
                Map.of("salary", -1), Map.of("salary", 10000000),
                Map.of("departmentId", 0), Map.of("jobId", 0))) {
            HttpResponse<String> response = request("PATCH", "/api/employees/1", input, null);
            assertEquals(400, response.statusCode(), response.body());
        }
        assertEquals(520000, get("/api/employees/1").path("salary").asInt());
    }

    // 「外部キー制約と不正形式入力の拒否」の検証
    @Test
    void enforcesForeignKeysAndRejectsMalformedInput() throws Exception {
        assertEquals(409, request("PATCH", "/api/employees/1",
                Map.of("departmentId", 999), null).statusCode());
        assertEquals(409, request("PATCH", "/api/employees/1",
                Map.of("jobId", 999), null).statusCode());
        assertEquals(400, request("PATCH", "/api/employees/1",
                Map.of("entranceDate", "invalid"), null).statusCode());
        assertEquals(400, request("GET",
                "/api/employees/search/by-salary?lowerSalary=invalid", null, null).statusCode());
        assertEquals(1, get("/api/employees/1").path("departmentId").asInt());
    }

    // 「PUTによる未存在リソースの登録防止」の検証
    @Test
    void doesNotCreateMissingResourcesThroughPut() throws Exception {
        HttpResponse<String> response = request("PUT", "/api/employees/999", employeeInput(), null);
        assertEquals(405, response.statusCode(), response.body());
        assertEquals(404, request("GET", "/api/employees/999", null, null).statusCode());
        assertEquals(404, request("PATCH", "/api/employees/999", Map.of("salary", 1), null).statusCode());
    }

    // 「静的クライアントへのETag・Location公開」の検証
    @Test
    void exposesEtagAndLocationToTheStaticClient() throws Exception {
        HttpRequest query = HttpRequest.newBuilder(uri("/api/employees/1"))
                .header("Origin", "http://localhost:5500").GET().build();
        HttpResponse<String> response = client.send(query, HttpResponse.BodyHandlers.ofString());
        assertEquals("http://localhost:5500",
                response.headers().firstValue("Access-Control-Allow-Origin").orElseThrow());
        String exposed = response.headers().firstValue("Access-Control-Expose-Headers").orElseThrow();
        assertTrue(exposed.contains("ETag"), exposed);
        assertTrue(exposed.contains("Location"), exposed);
    }

    // 「同一バージョンによる同時更新の排他制御」の検証
    @Test
    void allowsOnlyOneConcurrentUpdateWithTheSameVersion() throws Exception {
        String tag = etag(request("GET", "/api/employees/1", null, null));
        CountDownLatch start = new CountDownLatch(1);
        CompletableFuture<Integer> first = updateLater(start, tag, 400000);
        CompletableFuture<Integer> second = updateLater(start, tag, 410000);
        start.countDown();
        List<Integer> results = List.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
        assertEquals(1, results.stream().filter(code -> code == 200).count(), results.toString());
        assertEquals(1, results.stream().filter(code -> code == 409 || code == 412).count(),
                results.toString());
        assertEquals(1, jdbc.queryForObject(
                "SELECT VERSION FROM EMPLOYEE WHERE EMPLOYEE_ID = 1", Integer.class));
    }

    // 非同期での社員情報更新
    private CompletableFuture<Integer> updateLater(CountDownLatch start, String tag, int salary) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                assertTrue(start.await(5, TimeUnit.SECONDS));
                return request("PATCH", "/api/employees/1", Map.of("salary", salary), tag).statusCode();
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
        });
    }

    // 社員入力データの生成
    private Map<String, Object> employeeInput() {
        Map<String, Object> input = new HashMap<>();
        input.put("employeeName", "新入 社員");
        input.put("departmentId", 2);
        input.put("jobId", 1);
        input.put("salary", 300000);
        input.put("entranceDate", "2026-04-01");
        return input;
    }

    // 社員ID一覧の取得
    private List<Integer> ids(JsonNode page) {
        return page.path("_embedded").path("employees").valueStream()
                .map(employee -> employee.path("employeeId").asInt()).toList();
    }

    // ETagの取得
    private String etag(HttpResponse<String> response) {
        String tag = response.headers().firstValue("ETag").orElse(null);
        assertNotNull(tag, response.body());
        return tag;
    }

    // APIメソッド：主キー検索によるEmployee取得
    private JsonNode get(String path) throws Exception {
        HttpResponse<String> response = request("GET", path, null, null);
        assertEquals(200, response.statusCode(), response.body());
        return JSON.readTree(response.body());
    }

    // URIの生成
    private URI uri(String path) {
        String expanded = path.replaceAll("\\{[^}]*}", "");
        return URI.create(expanded.startsWith("http") ? expanded : "http://127.0.0.1:" + port + expanded);
    }

    // HTTPリクエストの送信
    private HttpResponse<String> request(String method, String path, Object body, String tag)
            throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri(path))
                .timeout(Duration.ofSeconds(5)).header("Accept", "application/hal+json");
        if (tag != null) {
            builder.header("If-Match", tag);
        }
        if (body != null) {
            builder.header("Content-Type", "application/json");
        }
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body)));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
