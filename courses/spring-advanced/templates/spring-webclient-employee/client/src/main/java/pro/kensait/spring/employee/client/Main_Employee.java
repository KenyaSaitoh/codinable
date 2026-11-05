package pro.kensait.spring.employee.client;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
/*
 * 社員APIをWebClientで呼び出すコンソールアプリケーション
 */
public class Main_Employee {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    public static void main(String[] args) throws Exception {
        WebClient client = WebClient.builder().baseUrl(ClientOptions.baseUrl(args)).build();
        readEmployees(client);
        searchEmployees(client);
        System.out.println("Summary => " + readAndCombine(client));
        System.out.println("Same department => " + readThenSearch(client));
        int deletedId = createUpdateDelete(client);
        readStream(client);
        readAsync(client);
        useHttpInterface(client, deletedId);
    }

    // 社員の取得
    private static void readEmployees(WebClient client) {
        // block()はコンソールの終了前に結果を確認するための待機WebFlux処理内では使わない
        EmployeeTO employee = client.get().uri("/employees/10001").retrieve()
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
                        .queryParam("departmentId", 3).build())
                .retrieve().bodyToFlux(EmployeeTO.class).collectList().block(TIMEOUT);
        System.out.println("Department => " + department);
        List<EmployeeTO> salary = client.get()
                .uri(builder -> builder.path("/employees/query_by_salary")
                        .queryParam("lowerSalary", 300000).queryParam("upperSalary", 400000).build())
                .retrieve().bodyToFlux(EmployeeTO.class).collectList().block(TIMEOUT);
        System.out.println("Salary => " + salary);
    }

    // 社員1件の取得と部署の検索を並行して送り、Mono.zip で合成する
    static String readAndCombine(WebClient client) {
        Mono<EmployeeTO> alice = client.get().uri("/employees/10001").retrieve() // 【1】
                .bodyToMono(EmployeeTO.class);
        Mono<List<EmployeeTO>> sales = client.get()
                .uri(builder -> builder.path("/employees/query_by_department")
                        .queryParam("departmentId", 3).build())
                .retrieve().bodyToFlux(EmployeeTO.class).collectList(); // 【2】
        return Mono.zip(alice, sales, // 【3】 両方を同時に購読し、両方そろったら合成する。片方が失敗すれば全体が失敗する
                (manager, members) -> manager.employeeName() + " / SALES " + members.size())
                .block(TIMEOUT);
    }

    // 社員を取得し、その結果の部署IDで次の検索を送る（前の結果に依存する要求は flatMap でつなぐ）
    static String readThenSearch(WebClient client) {
        return client.get().uri("/employees/10001").retrieve()
                .bodyToMono(EmployeeTO.class)
                .flatMap(employee -> client.get() // 【1】 前の結果を受け取ってから、次の要求の Mono を返す
                        .uri(builder -> builder.path("/employees/query_by_department")
                                .queryParam("departmentId", employee.departmentId()).build()) // 【2】
                        .retrieve().bodyToFlux(EmployeeTO.class).collectList()
                        .map(members -> employee.employeeName() + " / " + members.size())) // 【3】
                .block(TIMEOUT);
    }

    // 社員の登録・更新・削除（削除した社員のIDを返す）
    private static int createUpdateDelete(WebClient client) {
        EmployeeTO draft = new EmployeeTO(null, "Walter", 3, null,
                "ASSOCIATE", 230000, LocalDate.of(2018, 4, 1));
        ResponseEntity<EmployeeTO> created = Objects.requireNonNull(client.post().uri("/employees")
                .contentType(MediaType.APPLICATION_JSON).bodyValue(draft)
                .retrieve().toEntity(EmployeeTO.class).block(TIMEOUT));
        int id = Objects.requireNonNull(Objects.requireNonNull(created.getBody()).employeeId());
        System.out.println("Created => " + created.getStatusCode());
        System.out.println("Location => " + created.getHeaders().getLocation());
        try {
            EmployeeTO update = new EmployeeTO(id, "Walter", 1, null,
                    "ASSOCIATE", 250000, LocalDate.of(2018, 4, 1));
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
        return id;
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

    // 非同期の取得
    private static void readAsync(WebClient client) throws Exception {
        Mono<EmployeeTO> request = client.get().uri("/employees/10001").retrieve()
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

    // HTTP インタフェース（@HttpExchange）で同じ社員 API を呼び、削除済みの社員の 404 を想定した結果へ変える
    static void useHttpInterface(WebClient client, int deletedId) {
        EmployeeHttpClient api = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(client)) // 【1】
                .build()
                .createClient(EmployeeHttpClient.class); // 【2】
        System.out.println("Http Interface => " + api.find(10001).block(TIMEOUT));
        System.out.println("Http Interface SALES => " + api.byDepartment(3).block(TIMEOUT).size());

        Optional<EmployeeTO> lookup = api.find(deletedId)
                .onErrorResume(WebClientResponseException.NotFound.class, error -> Mono.empty()) // 【3】 404 だけを「無い」に変える
                .blockOptional(TIMEOUT);
        if (lookup.isEmpty()) {
            System.out.println("Expected error => 404");
        }
    }
}
