package pro.kensait.jakartaee.employee.client;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

/*
 * 社員APIを非同期に呼ぶ Java コンソール
 * Jakarta REST のクライアントAPI の rx() と、MicroProfile Rest Client の非同期メソッドを使う
 * 404 以外のエラーや通信の失敗は main まで伝わり、0 以外の終了コードで終わる
 */
public class Main_Employee {

    public static void main(String[] args) throws Exception {
        Client client = ClientBuilder.newBuilder() // 【1】
                .connectTimeout(2, TimeUnit.SECONDS) // 【2】
                .readTimeout(3, TimeUnit.SECONDS)
                .build();
        try {
            WebTarget employees = client.target(ClientOptions.baseUrl(args)) // 【3】
                    .path("employees");
            readAndCombine(employees);
            int deletedId = createUpdateDelete(employees);
            useRestClient(args, deletedId);
        } finally {
            client.close();
        }
    }

    // 社員1件の取得と部署の検索を並行して送り、thenCombine で合成する
    private static void readAndCombine(WebTarget employees) throws Exception {
        CompletionStage<EmployeeTO> alice = employees.path("{id}") // 【1】
                .resolveTemplate("id", 10001)
                .request(MediaType.APPLICATION_JSON) // 【2】
                .rx() // 【3】
                .get(EmployeeTO.class); // 【4】
        CompletionStage<String> name = alice.thenApply(EmployeeTO::employeeName); // 【5】

        CompletionStage<List<EmployeeTO>> sales = employees.path("query_by_department")
                .queryParam("departmentId", 3) // 【1】
                .request(MediaType.APPLICATION_JSON)
                .rx().get(new GenericType<List<EmployeeTO>>() {}); // 【2】
        CompletionStage<String> summary = alice.thenCombine(sales, // 【3】
                (manager, members) -> manager.employeeName() + " / SALES " + members.size());
        System.out.println("Summary => " + summary.toCompletableFuture().get(5, TimeUnit.SECONDS)); // 【4】
        System.out.println("Name => " + name.toCompletableFuture().get(5, TimeUnit.SECONDS));
    }

    // 登録の応答で採番 ID を受け取ってから、thenCompose で更新を送る。最後に必ず削除する
    private static int createUpdateDelete(WebTarget employees) throws Exception {
        EmployeeTO draft = new EmployeeTO(null, "Walter", 3, null, "ASSOCIATE", 230000,
                LocalDate.of(2018, 4, 1));
        CompletionStage<Integer> createdId = employees.request(MediaType.APPLICATION_JSON)
                .rx().post(Entity.json(draft)) // 【1】
                .thenApply(response -> {
                    try (response) { // 【2】
                        if (response.getStatus() != 201) {
                            throw new IllegalStateException("登録に失敗しました: " + response.getStatus());
                        }
                        System.out.println("Location => " + response.getLocation());
                        return response.readEntity(EmployeeTO.class).employeeId(); // 【3】
                    }
                });
        CompletionStage<EmployeeTO> updated = createdId.thenCompose(id -> // 【4】
                employees.path("{id}").resolveTemplate("id", id)
                        .request(MediaType.APPLICATION_JSON)
                        .rx().put(Entity.json(draft.withId(id).withSalary(250000)), EmployeeTO.class));

        int id = createdId.toCompletableFuture().get(5, TimeUnit.SECONDS);
        try {
            System.out.println("Updated => " + updated.toCompletableFuture().get(5, TimeUnit.SECONDS));
        } finally {
            // 更新が失敗しても、自分で登録した社員は削除を試みる
            try (Response deleted = employees.path("{id}").resolveTemplate("id", id)
                    .request().rx().delete().toCompletableFuture().get(5, TimeUnit.SECONDS)) {
                System.out.println("Deleted => " + deleted.getStatus());
            }
        }
        return id;
    }

    // MicroProfile Rest Client で同じ社員APIを呼び、削除済みの社員の 404 を想定した結果へ変える
    private static void useRestClient(String[] args, int deletedId) throws Exception {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            EmployeeApi api = RestClientBuilder.newBuilder()
                    .baseUri(URI.create(ClientOptions.baseUrl(args))) // 【4】
                    .connectTimeout(2, TimeUnit.SECONDS)
                    .readTimeout(3, TimeUnit.SECONDS)
                    .executorService(executor) // 【5】
                    .build(EmployeeApi.class);
            System.out.println("Rest Client => "
                    + api.findAsync(10001).toCompletableFuture().get(5, TimeUnit.SECONDS));
            System.out.println("Rest Client SALES => "
                    + api.byDepartmentAsync(3).toCompletableFuture().get(5, TimeUnit.SECONDS).size());

            CompletionStage<Optional<EmployeeTO>> lookup = api.findAsync(deletedId)
                    .thenApply(Optional::of) // 【1】
                    .exceptionally(error -> { // 【2】
                        Throwable cause = error instanceof CompletionException ? error.getCause() : error;
                        if (cause instanceof WebApplicationException e
                                && e.getResponse().getStatus() == 404) {
                            return Optional.empty(); // 【3】
                        }
                        throw new CompletionException(cause); // 【4】
                    });
            if (lookup.toCompletableFuture().get(5, TimeUnit.SECONDS).isEmpty()) {
                System.out.println("Expected error => 404");
            }
        }
    }
}
