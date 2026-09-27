package pro.kensait.spring.employee.rest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import pro.kensait.spring.employee.rest.entity.Department;
import pro.kensait.spring.employee.rest.entity.Employee;
import pro.kensait.spring.employee.rest.entity.Job;
import pro.kensait.spring.employee.rest.service.EmployeePage;

/*
 * 社員APIのテスト
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:hsqldb:mem:employee_rest_tests")
@ActiveProfiles("demo")
@Sql(scripts = {"/reset-employees.sql", "/db/3_EMPLOYEE_DML.sql"})
@Timeout(30)
class EmployeeApiTest {
    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    private RestClient client;

    // テスト前処理
    @BeforeEach
    void connect() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        client = RestClient.builder().baseUrl("http://127.0.0.1:" + port)
                .requestFactory(factory).build();
    }

    // 「社員・部署・役職エンティティの構成」の検証
    @Test
    void hasExactlyEmployeeDepartmentAndJobEntities() {
        assertEquals(Set.of("Employee", "Department", "Job"),
                entityManagerFactory.getMetamodel().getEntities().stream()
                        .map(entity -> entity.getName()).collect(Collectors.toSet()));
    }

    // 「部署・役職マスターの表示順」の検証
    @Test
    void exposesDepartmentAndJobMastersInMvcOrder() {
        Department[] departments = client.get().uri("/departments").retrieve().body(Department[].class);
        Job[] jobs = client.get().uri("/jobs").retrieve().body(Job[].class);
        assertEquals(4, departments.length);
        assertEquals(4, jobs.length);
        assertEquals("PLANNING", departments[0].getDepartmentName());
        assertEquals("TOKYO HQ", departments[0].getLocation());
        assertEquals(List.of(1, 2, 3, 4), Arrays.stream(jobs).map(Job::getGrade).toList());
    }

    // 「在籍社員を5件ずつ取得するページング」の検証
    @Test
    void pagesFiveActiveEmployeesAtATime() {
        EmployeePage first = page("/employees");
        assertEquals(16, first.totalElements());
        assertEquals(4, first.totalPages());
        assertEquals(List.of(10001, 10002, 10003, 10004, 10005), ids(first));
        assertEquals(List.of(10006, 10007, 10008, 10009, 10010), ids(page("/employees?page=2")));
        assertEquals(List.of(10016), ids(page("/employees?page=4")));
        assertEquals(ids(first), ids(page("/employees?page=0")));
        assertTrue(page("/employees?page=5").content().isEmpty());
    }

    // 「キーワード・部署・役職・月給範囲の複合検索」の検証
    @Test
    void combinesKeywordDepartmentJobAndInclusiveSalaryFilters() {
        EmployeePage keyword = client.get().uri(builder -> builder.path("/employees")
                .queryParam("keyword", " Alice ").build()).retrieve().body(EmployeePage.class);
        assertEquals(List.of(10001), ids(keyword));
        assertEquals(List.of(10011), ids(page(
                "/employees?departmentId=4&jobId=2&salaryFrom=320000&salaryTo=320000")));
        assertEquals(List.of(10001, 10002, 10003, 10004, 10005), ids(page(
                "/employees?salaryFrom=300000&salaryTo=500000")));
        assertEquals(0, page("/employees?departmentId=999").totalElements());
    }

    // 「不正な検索条件に対する400応答」の検証
    @Test
    void reportsInvalidSearchAs400() {
        assertEquals(400, error(() -> page("/employees?salaryFrom=500000&salaryTo=300000")));
        assertEquals(400, error(() -> page("/employees?departmentId=invalid")));
        assertEquals(400, error(() -> page("/employees?page=invalid")));
        assertEquals(400, status(HttpMethod.GET, "/employees/query_by_salary", null));
    }

    // 「JPAによる社員全項目の取得」の検証
    @Test
    void retrievesAllEmployeeFieldsFromJpa() {
        Employee employee = get(10001);
        assertEquals("E10001", employee.getEmployeeCode());
        assertEquals(3, employee.getDepartmentId());
        assertEquals(4, employee.getJobId());
        assertEquals(LocalDate.of(2012, 4, 1), employee.getEntranceDate());
        assertEquals("active", employee.getStatus());
        assertEquals(0, employee.getVersion());
    }

    // 「サーバー採番項目を使用した社員登録」の検証
    @Test
    void createsEmployeeWithServerIdCodeAndVersion() {
        Map<String, Object> input = input();
        input.put("employeeName", "  Walter  ");
        ResponseEntity<Employee> result = client.post().uri("/employees").body(input)
                .retrieve().toEntity(Employee.class);
        Employee created = result.getBody();
        assertEquals(201, result.getStatusCode().value());
        assertEquals("/employees/10017", result.getHeaders().getLocation().toString());
        assertEquals(10017, created.getEmployeeId());
        assertEquals("E10017", created.getEmployeeCode());
        assertEquals("Walter", created.getEmployeeName());
        assertEquals("active", created.getStatus());
        assertEquals(0, created.getVersion());
        assertEquals("E10017", get(10017).getEmployeeCode());
        assertEquals(17, page("/employees").totalElements());
    }

    // 「不正・未入力項目の拒否と未保存」の検証
    @Test
    void rejectsInvalidOrMissingEmployeeFieldsWithoutWriting() {
        Map<String, Object> invalidValues = Map.of("employeeName", " ", "departmentId", 999,
                "jobId", 999, "salary", -1, "entranceDate", "invalid-date");
        for (var field : invalidValues.entrySet()) {
            Map<String, Object> invalid = input();
            invalid.put(field.getKey(), field.getValue());
            assertEquals(400, status(HttpMethod.POST, "/employees", invalid), field.getKey());
            invalid.remove(field.getKey());
            assertEquals(400, status(HttpMethod.POST, "/employees", invalid), field.getKey());
        }
        assertEquals(16, jdbc.queryForObject("SELECT COUNT(*) FROM EMPLOYEE", Integer.class));
    }

    // 「氏名・月給・整数項目の入力制約」の検証
    @Test
    void enforcesNameAndSalaryLimitsAndIntegerValues() {
        for (Object invalidSalary : List.of(10_000_000, 100.5)) {
            Map<String, Object> invalid = input();
            invalid.put("salary", invalidSalary);
            assertEquals(400, status(HttpMethod.POST, "/employees", invalid));
        }
        Map<String, Object> invalidName = input();
        invalidName.put("employeeName", "a".repeat(31));
        assertEquals(400, status(HttpMethod.POST, "/employees", invalidName));
        Map<String, Object> boundary = input();
        boundary.put("employeeName", "a".repeat(30));
        boundary.put("salary", 0);
        assertEquals(201, status(HttpMethod.POST, "/employees", boundary));
        boundary.put("salary", 9_999_999);
        assertEquals(201, status(HttpMethod.POST, "/employees", boundary));
    }

    // 「サーバー管理項目の更新禁止」の検証
    @Test
    void cannotSetServerOwnedFields() {
        for (String field : List.of("employeeId", "employeeCode", "status", "departmentName")) {
            Map<String, Object> invalid = input();
            invalid.put(field, "untrusted");
            assertEquals(400, status(HttpMethod.POST, "/employees", invalid), field);
        }
        assertEquals("Alice", get(10001).getEmployeeName());
    }

    // 「社員更新と古いバージョンの拒否」の検証
    @Test
    void updatesFieldsAndRejectsAnOutdatedVersion() {
        Map<String, Object> request = input();
        request.put("version", 0);
        Employee updated = client.put().uri("/employees/10001").body(request).retrieve().body(Employee.class);
        assertEquals(10001, updated.getEmployeeId());
        assertEquals("E10001", updated.getEmployeeCode());
        assertEquals(1, updated.getVersion());
        assertEquals(1, updated.getDepartmentId());
        assertEquals(3, updated.getJobId());
        assertEquals(520000, updated.getSalary());
        request.put("salary", 540000);
        assertEquals(409, status(HttpMethod.PUT, "/employees/10001", request));
        assertEquals(520000, get(10001).getSalary());
    }

    // 「更新時のバージョン必須と未存在社員の登録防止」の検証
    @Test
    void updateRequiresVersionAndNeverCreatesMissingEmployee() {
        assertEquals(400, status(HttpMethod.PUT, "/employees/10001", input()));
        Map<String, Object> request = input();
        request.put("version", 0);
        assertEquals(404, status(HttpMethod.PUT, "/employees/999", request));
        assertEquals(404, status(HttpMethod.GET, "/employees/999", null));
        assertEquals(404, status(HttpMethod.DELETE, "/employees/999", null));
        assertEquals(16, page("/employees").totalElements());
    }

    // 「論理削除した社員の全参照APIからの除外」の検証
    @Test
    void logicallyDeletesAndExcludesFromEveryReadEndpoint() {
        assertEquals(204, status(HttpMethod.DELETE, "/employees/10001", null));
        assertEquals("deleted", jdbc.queryForObject(
                "SELECT STATUS FROM EMPLOYEE WHERE EMPLOYEE_ID=10001", String.class));
        assertEquals(16, jdbc.queryForObject("SELECT COUNT(*) FROM EMPLOYEE", Integer.class));
        assertEquals(404, status(HttpMethod.GET, "/employees/10001", null));
        assertEquals(404, status(HttpMethod.DELETE, "/employees/10001", null));
        assertEquals(15, page("/employees").totalElements());
        Employee[] found = client.get().uri("/employees/query_by_salary?lowerSalary=0")
                .retrieve().body(Employee[].class);
        assertEquals(15, found.length);
        assertFalse(Arrays.stream(found).anyMatch(employee -> employee.getEmployeeId() == 10001));
    }

    // 「既存の月給検索APIの維持」の検証
    @Test
    void keepsTheExistingSalaryEndpoint() {
        Employee[] found = client.get().uri("/employees/query_by_salary?lowerSalary=450000")
                .retrieve().body(Employee[].class);
        assertEquals(List.of(10001, 10002, 10007, 10008), Arrays.stream(found).map(Employee::getEmployeeId).toList());
        assertEquals(400, status(HttpMethod.GET, "/employees/query_by_salary?lowerSalary=-1", null));
    }

    // 「構造化されたエラーレスポンス」の検証
    @Test
    void returnsStructuredErrors() {
        RestClientResponseException missing = assertThrows(RestClientResponseException.class, () -> get(999));
        ApiExceptionHandler.ApiError error = missing.getResponseBodyAs(ApiExceptionHandler.ApiError.class);
        assertEquals(404, error.status());
        assertNotNull(error.message());
        assertNotNull(error.errors());
        RestClientResponseException invalid = assertThrows(RestClientResponseException.class,
                () -> client.post().uri("/employees").body(Map.of()).retrieve().body(Employee.class));
        assertEquals(400, invalid.getStatusCode().value());
        assertFalse(invalid.getResponseBodyAs(ApiExceptionHandler.ApiError.class).errors().isEmpty());
    }

    // 「同一バージョンによる同時更新の排他制御」の検証
    @Test
    void acceptsOnlyOneOfTwoConcurrentUpdatesWithTheSameVersion() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        CompletableFuture<Integer> first = updateConcurrently(start, 530000);
        CompletableFuture<Integer> second = updateConcurrently(start, 540000);
        start.countDown();
        assertEquals(Set.of(200, 409), Set.of(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS)));
        assertEquals(1, get(10001).getVersion());
        assertTrue(Set.of(530000, 540000).contains(get(10001).getSalary()));
    }

    // concurrentlyの更新
    private CompletableFuture<Integer> updateConcurrently(CountDownLatch start, int salary) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                assertTrue(start.await(5, TimeUnit.SECONDS));
                Map<String, Object> request = input();
                request.put("version", 0);
                request.put("salary", salary);
                return status(HttpMethod.PUT, "/employees/10001", request);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(exception);
            }
        });
    }

    // 入力の実行
    private Map<String, Object> input() {
        return new HashMap<>(Map.of("employeeName", "Alice", "departmentId", 1,
                "jobId", 3, "salary", 520000, "entranceDate", "2012-04-01"));
    }

    // APIメソッド：主キー検索によるEmployee取得
    private Employee get(int id) {
        return client.get().uri("/employees/{id}", id).retrieve().body(Employee.class);
    }

    // ページの実行
    private EmployeePage page(String uri) {
        return client.get().uri(uri).retrieve().body(EmployeePage.class);
    }

    // 社員ID一覧の取得
    private List<Integer> ids(EmployeePage page) {
        return page.content().stream().map(Employee::getEmployeeId).toList();
    }

    // エラーレスポンスの生成
    private int error(Runnable request) {
        return assertThrows(RestClientResponseException.class, request::run).getStatusCode().value();
    }

    // 状態の実行
    private int status(HttpMethod method, String uri, Object body) {
        RestClient.RequestBodySpec request = client.method(method).uri(uri);
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).body(body);
        }
        return request.retrieve().onStatus(code -> code.isError(), (req, response) -> { })
                .toBodilessEntity().getStatusCode().value();
    }
}
