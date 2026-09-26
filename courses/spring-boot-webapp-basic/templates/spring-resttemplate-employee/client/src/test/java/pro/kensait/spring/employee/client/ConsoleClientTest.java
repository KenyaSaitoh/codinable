package pro.kensait.spring.employee.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
/*
 * consoleクライアントのテスト
 */
@Timeout(20)
class ConsoleClientTest {
    private HttpServer server;
    private String baseUrl;
    private final List<String> requests = new CopyOnWriteArrayList<>();
    private volatile int activeId;
    private int nextId = 7;
    private volatile boolean failRead;
    private volatile boolean failUpdate;
    private volatile boolean failDelete;
    private volatile boolean failMissing;
    private volatile String updateBody = "";

    // テスト前処理
    @BeforeEach
    void startApi() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/employees", this::handle);
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    // テスト後処理
    @AfterEach
    void stopApi() {
        server.stop(0);
    }

    // 「自身が登録した社員だけを変更する再実行」の検証
    @Test
    void canRunTwiceAndOnlyMutatesOwnCreatedEmployees() throws Exception {
        Main_Employee.main(new String[] {baseUrl});
        Main_Employee.main(new String[] {baseUrl + "/"});
        assertEquals(0, activeId);
        assertEquals(2, requests.stream().filter("POST /employees"::equals).count());
        for (int id : new int[] {7, 8}) {
            assertTrue(requests.contains("PUT /employees/" + id));
            assertTrue(requests.contains("DELETE /employees/" + id));
            assertTrue(requests.contains("GET /employees/" + id));
        }
        assertFalse(requests.contains("PUT /employees/1"));
        assertFalse(requests.contains("DELETE /employees/6"));
        assertTrue(updateBody.contains("\"salary\":460000"));
        assertTrue(updateBody.contains("\"departmentId\":20"));
        assertTrue(requests.contains("GET /employees/query_by_department?departmentId=10"));
        assertTrue(requests.contains(
                "GET /employees/query_by_salary?lowerSalary=300000&upperSalary=400000"));
    }

    // 「上流障害を成功として扱わない制御」の検証
    @Test
    void upstreamFailureIsNotReportedAsSuccess() {
        failRead = true;
        assertThrows(Exception.class, () -> Main_Employee.main(new String[] {baseUrl}));
        assertFalse(requests.contains("POST /employees"));
    }

    // 「更新失敗時に登録済み社員を削除する後処理」の検証
    @Test
    void cleansUpCreatedEmployeeIfUpdateFails() {
        failUpdate = true;
        assertThrows(Exception.class, () -> Main_Employee.main(new String[] {baseUrl}));
        assertTrue(requests.contains("PUT /employees/7"));
        assertTrue(requests.contains("DELETE /employees/7"));
        assertEquals(0, activeId);
    }

    // 「後処理失敗の通知」の検証
    @Test
    void reportsCleanupFailure() {
        failDelete = true;
        assertThrows(Exception.class, () -> Main_Employee.main(new String[] {baseUrl}));
        assertTrue(requests.contains("DELETE /employees/7"));
        assertEquals(7, activeId);
    }

    // 「404だけを想定した未存在社員として扱う制御」の検証
    @Test
    void onlyTreats404AsTheExpectedMissingEmployee() {
        failMissing = true;
        assertThrows(Exception.class, () -> Main_Employee.main(new String[] {baseUrl}));
        assertEquals(0, activeId);
    }

    // consoleクライアントの処理
    private void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            requests.add(method + " " + exchange.getRequestURI());
            if ("GET".equals(method)) {
                get(exchange, path);
            } else if ("POST".equals(method) && "/employees".equals(path)) {
                activeId = nextId++;
                exchange.getResponseHeaders().add("Location", baseUrl + "/employees/" + activeId);
                respond(exchange, 201, employee(activeId));
            } else if ("PUT".equals(method) && path.equals("/employees/" + activeId)) {
                updateBody = body;
                respond(exchange, failUpdate ? 500 : 200,
                        failUpdate ? "{\"error\":\"update failed\"}" : employee(activeId));
            } else if ("DELETE".equals(method) && path.equals("/employees/" + activeId)) {
                if (failDelete) {
                    respond(exchange, 503, "{\"error\":\"delete failed\"}");
                } else {
                    activeId = 0;
                    respond(exchange, 204, "");
                }
            } else {
                respond(exchange, 400, "{\"error\":\"unexpected request\"}");
            }
        }
    }

    // データの取得
    private void get(HttpExchange exchange, String path) throws IOException {
        if (failRead) {
            respond(exchange, 503, "{\"error\":\"unavailable\"}");
        } else if ("/employees".equals(path) || path.startsWith("/employees/query_by_")) {
            respond(exchange, 200, "[" + employee(1) + "]");
        } else if ("/employees/1".equals(path)) {
            respond(exchange, 200, employee(1));
        } else if (activeId != 0 && path.equals("/employees/" + activeId)) {
            respond(exchange, 200, employee(activeId));
        } else {
            respond(exchange, failMissing ? 503 : 404, "{\"error\":\"missing\"}");
        }
    }

    // 引数は@Argumentによりスキーマの引数「id」からバインドされる）
    private String employee(int id) {
        return "{\"employeeId\":" + id + ",\"employeeName\":\"Sample\","
                + "\"departmentId\":10,\"departmentName\":\"Sales\",\"jobName\":\"Sales\","
                + "\"salary\":300000,\"entranceDate\":\"2021-04-01\"}";
    }

    // respondの実行
    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, status == 204 ? -1 : bytes.length);
        if (status != 204) {
            exchange.getResponseBody().write(bytes);
        }
    }
}
