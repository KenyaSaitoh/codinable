package pro.kensait.spring.employee.client;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
/*
 * main社員の機能を提供するクラス
 */
public class Main_Employee {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    public static void main(String[] args) throws Exception {
        WebClient client = WebClient.builder().baseUrl(ClientOptions.baseUrl(args)).build();
        readEmployees(client);
        searchEmployees(client);
        createUpdateDelete(client);
        readStream(client);
        readAsync(client);
    }

    // 社員の取得
    private static void readEmployees(WebClient client) {
        // block()はコンソールの終了前に結果を確認するための待機WebFlux処理内では使わない
        EmployeeTO employee = client.get().uri("/employees/1").retrieve()
                .bodyToMono(EmployeeTO.class).block(TIMEOUT);
        System.out.println("Employee => " + employee);
        List<EmployeeTO> employees = client.get().uri("/employees").retrieve()
                .bodyToFlux(EmployeeTO.class).collectList().block(TIMEOUT);
        System.out.println("Employees => " + employees);
    }

    // 社員の検索
    private static void searchEmployees(WebClient client) {
        List<EmployeeTO> department = client.get()
                .uri(builder -> builder.path("/employees/query_by_department")
                        .queryParam("departmentId", 10).build())
                .retrieve().bodyToFlux(EmployeeTO.class).collectList().block(TIMEOUT);
        System.out.println("Department => " + department);
        List<EmployeeTO> salary = client.get()
                .uri(builder -> builder.path("/employees/query_by_salary")
                        .queryParam("lowerSalary", 300000).queryParam("upperSalary", 400000).build())
                .retrieve().bodyToFlux(EmployeeTO.class).collectList().block(TIMEOUT);
        System.out.println("Salary => " + salary);
    }

    // updatedeleteの生成
    private static void createUpdateDelete(WebClient client) {
        EmployeeTO draft = new EmployeeTO(null, "CLI sample", 10, null,
                "Sales", 300000, LocalDate.of(2021, 4, 1));
        ResponseEntity<EmployeeTO> created = Objects.requireNonNull(client.post().uri("/employees")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(draft)
                .retrieve().toEntity(EmployeeTO.class).block(TIMEOUT));
        int id = Objects.requireNonNull(Objects.requireNonNull(created.getBody()).employeeId());
        System.out.println("Created => " + created.getStatusCode());
        System.out.println("Location => " + created.getHeaders().getLocation());
        try {
            EmployeeTO update = new EmployeeTO(id, "CLI sample updated", 20, null,
                    "Engineer", 460000, LocalDate.of(2021, 4, 1));
            EmployeeTO updated = client.put().uri("/employees/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON).bodyValue(update)
                    .retrieve().bodyToMono(EmployeeTO.class).block(TIMEOUT);
            System.out.println("Updated => " + updated);
        } finally {
            ResponseEntity<Void> deleted = Objects.requireNonNull(client.delete()
                    .uri("/employees/{id}", id).retrieve().toBodilessEntity().block(TIMEOUT));
            System.out.println("Deleted => " + deleted.getStatusCode());
        }
        showNotFound(client, id);
    }

    // 非検出の表示
    private static void showNotFound(WebClient client, int deletedId) {
        try {
            client.get().uri("/employees/{id}", deletedId).retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> response.createException())
                    .bodyToMono(EmployeeTO.class).block(TIMEOUT);
            throw new IllegalStateException("削除済み社員のGETで404が返りませんでした");
        } catch (WebClientResponseException error) {
            if (error.getStatusCode().value() != 404) {
                throw error;
            }
            System.out.println("Expected error => " + error.getStatusCode());
            System.out.println("Body => " + error.getResponseBodyAsString());
        }
    }

    // ストリームの取得
    private static void readStream(WebClient client) {
        // Streamを閉じて購読を解放する配列JSONの取得はSSEのストリーミングとは異なる
        try (Stream<EmployeeTO> employees = client.get().uri("/employees").retrieve()
                .bodyToFlux(EmployeeTO.class).timeout(TIMEOUT).toStream()) {
            employees.forEach(employee -> System.out.println("Stream Employee => " + employee));
        }
    }

    // asyncの取得
    private static void readAsync(WebClient client) throws Exception {
        Mono<EmployeeTO> request = client.get().uri("/employees/1").retrieve()
                .bodyToMono(EmployeeTO.class).timeout(TIMEOUT);
        CompletableFuture<EmployeeTO> result = new CompletableFuture<>();
        Disposable subscription = request.subscribe(result::complete, result::completeExceptionally);
        System.out.println("Async request started");
        try {
            // 固定時間sleepせず、成功か失敗が届くまで待つ失敗はmainにも伝える
            System.out.println("Async Employee => " + result.get(5, TimeUnit.SECONDS));
        } finally {
            subscription.dispose();
        }
    }
}
