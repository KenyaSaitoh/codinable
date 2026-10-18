package pro.kensait.jakartaee.employee.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.SseEventSource;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/*
 * 社員一覧の SSE（GET /api/employees/stream）を Jakarta REST のクライアントAPI の SseEventSource で受信し、件数を確かめる
 * complete を受け取ったら正常終了、届かずに切れたら異常終了（終了コード 1）
 */
public class EmployeeStreamClient {

    public record EmployeeView(Integer employeeId, String employeeName, Integer departmentId,
            String departmentName, String jobName, Integer salary, LocalDate entranceDate) {
    }

    public static void main(String[] args) throws Exception {
        String baseUri = args.length > 0 ? args[0] : "http://localhost:8080/jakarta-async-employee/api";
        List<EmployeeView> received = new CopyOnWriteArrayList<>();
        boolean[] completed = {false};
        Client client = ClientBuilder.newClient();
        try {
            WebTarget target = client.target(baseUri).path("employees/stream");
            CountDownLatch finished = new CountDownLatch(1);
            try (SseEventSource source = SseEventSource.target(target).build()) { // 【1】
                source.register(event -> { // 【2】
                    if ("complete".equals(event.getName())) { // 【3】
                        completed[0] = true;
                        finished.countDown();
                        return;
                    }
                    EmployeeView employee = event.readData(EmployeeView.class,
                            MediaType.APPLICATION_JSON_TYPE);
                    received.add(employee);
                    System.out.println("Received => " + employee.employeeId() + " " + employee.employeeName());
                }, error -> finished.countDown()); // 【4】
                source.open(); // 【5】
                finished.await(30, TimeUnit.SECONDS);
            }
        } finally {
            client.close();
        }
        System.out.println("Count => " + received.size() + (completed[0] ? "（complete を受け取りました）" : "（complete が届いていません）"));
        if (!completed[0]) {
            System.exit(1);
        }
    }
}
