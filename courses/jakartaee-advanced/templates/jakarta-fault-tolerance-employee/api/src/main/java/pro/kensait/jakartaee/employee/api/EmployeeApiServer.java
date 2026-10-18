package pro.kensait.jakartaee.employee.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/*
 * 確認用の社員API（http://localhost:8095）。Payara Server とは別のプロセスとして動く
 * ▶ アプリ実行で一緒に起動し、出力欄の右の「api」の停止・起動で障害と復旧を作れる
 *   GET  /employees              … 社員 16 件
 *   GET  /employees/{employeeId} … 社員 1 件（無ければ 404）
 *   POST /admin/delay?ms=1500    … 以後の /employees の応答を指定のミリ秒だけ遅らせる（0 で元に戻す）
 * データはメモリ内にあり、起動し直すと初期状態に戻る
 */
public class EmployeeApiServer {
    private static final Jsonb JSONB = JsonbBuilder.create();
    private static final Map<Integer, Employee> EMPLOYEES = new TreeMap<>();
    private static final AtomicLong DELAY_MS = new AtomicLong();

    public record Employee(Integer employeeId, String employeeName, Integer departmentId,
            String departmentName, String jobName, Integer salary, LocalDate entranceDate) {
    }

    private static void add(int id, String name, Integer departmentId, String departmentName,
            String jobName, int salary, LocalDate entranceDate) {
        EMPLOYEES.put(id, new Employee(id, name, departmentId, departmentName, jobName, salary, entranceDate));
    }

    static {
        add(10001, "Alice", 3, "SALES", "MANAGER", 500000, LocalDate.of(2012, 4, 1));
        add(10002, "Bob", 1, "PLANNING", "MANAGER", 450000, LocalDate.of(2012, 4, 1));
        add(10003, "Carol", 2, "HR", "CHIEF", 350000, LocalDate.of(2012, 4, 1));
        add(10004, "Dave", 3, "SALES", "LEADER", 400000, LocalDate.of(2012, 4, 1));
        add(10005, "Ellen", 3, "SALES", "CHIEF", 300000, LocalDate.of(2013, 4, 1));
        add(10006, "Frank", 1, "PLANNING", "ASSOCIATE", 250000, LocalDate.of(2013, 10, 1));
        add(10007, "Ivan", 4, "PRODUCT", "MANAGER", 480000, LocalDate.of(2014, 1, 1));
        add(10008, "Justin", 2, "HR", "MANAGER", 460000, LocalDate.of(2014, 4, 1));
        add(10009, "Mallory", 4, "PRODUCT", "LEADER", 420000, LocalDate.of(2014, 7, 1));
        add(10010, "Matilda", 3, "SALES", "ASSOCIATE", 280000, LocalDate.of(2015, 8, 1));
        add(10011, "Oscar", 4, "PRODUCT", "CHIEF", 320000, LocalDate.of(2015, 11, 1));
        add(10012, "Pat", 4, "PRODUCT", "ASSOCIATE", 240000, LocalDate.of(2016, 4, 1));
        add(10013, "Peggy", 3, "SALES", "ASSOCIATE", 270000, LocalDate.of(2016, 10, 1));
        add(10014, "Victor", null, null, "ASSOCIATE", 220000, LocalDate.of(2017, 4, 1));
        add(10015, "Steve", 1, "PLANNING", "LEADER", 380000, LocalDate.of(2017, 10, 1));
        add(10016, "Trent", 4, "PRODUCT", "CHIEF", 310000, LocalDate.of(2017, 10, 1));
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8095;
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/employees", EmployeeApiServer::employees);
        server.createContext("/admin/delay", EmployeeApiServer::delay);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
        System.out.println("Employee API started on http://localhost:" + port + "/employees");
    }

    private static void employees(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        System.out.println("GET " + path + " delay=" + DELAY_MS.get() + "ms");
        sleep(DELAY_MS.get());
        if (path.equals("/employees") || path.equals("/employees/")) {
            send(exchange, 200, JSONB.toJson(List.copyOf(EMPLOYEES.values())));
            return;
        }
        try {
            Employee employee = EMPLOYEES.get(Integer.parseInt(path.substring("/employees/".length())));
            if (employee == null) {
                send(exchange, 404, "{\"error\":\"Employee not found\"}");
            } else {
                send(exchange, 200, JSONB.toJson(employee));
            }
        } catch (NumberFormatException e) {
            send(exchange, 400, "{\"error\":\"Invalid employee id\"}");
        }
    }

    private static void delay(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        String query = uri.getQuery() == null ? "" : uri.getQuery();
        long ms = query.startsWith("ms=") ? Long.parseLong(query.substring(3)) : 0;
        DELAY_MS.set(ms);
        System.out.println("delay set to " + ms + "ms");
        send(exchange, 200, "{\"delayMs\":" + ms + "}");
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
