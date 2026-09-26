package pro.kensait.spring.employee.client;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
/*
 * main社員の機能を提供するクラス
 */
public class Main_Employee {
    public static void main(String[] args) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));
        RestClient client = RestClient.builder().baseUrl(ClientOptions.baseUrl(args))
                .requestFactory(factory).build();
        readEmployees(client);
        searchEmployees(client);
        createUpdateDelete(client);
    }

    // 社員の取得
    private static void readEmployees(RestClient client) {
        // body()はJSONをDTOへ変換するtoEntity()ならステータスとヘッダーも参照できる
        EmployeeTO employee = client.get().uri("/employees/1").retrieve().body(EmployeeTO.class);
        System.out.println("Employee => " + employee);
        ResponseEntity<EmployeeTO> response = client.get().uri("/employees/1")
                .retrieve().toEntity(EmployeeTO.class);
        System.out.println("Status => " + response.getStatusCode());
        System.out.println("Headers => " + response.getHeaders());
        // ジェネリック型の情報を保持して、配列ではなくListとして受け取る
        List<EmployeeTO> employees = client.get().uri("/employees").retrieve()
                .body(new ParameterizedTypeReference<List<EmployeeTO>>() { });
        System.out.println("Employees => " + employees);
    }

    // 社員の検索
    private static void searchEmployees(RestClient client) {
        List<EmployeeTO> department = client.get()
                .uri(builder -> builder.path("/employees/query_by_department")
                        .queryParam("departmentId", 10).build())
                .retrieve().body(new ParameterizedTypeReference<List<EmployeeTO>>() { });
        System.out.println("Department => " + department);
        List<EmployeeTO> salary = client.get()
                .uri(builder -> builder.path("/employees/query_by_salary")
                        .queryParam("lowerSalary", 300000).queryParam("upperSalary", 400000).build())
                .retrieve().body(new ParameterizedTypeReference<List<EmployeeTO>>() { });
        System.out.println("Salary => " + salary);
    }

    // updatedeleteの生成
    private static void createUpdateDelete(RestClient client) {
        // 既存社員には書き込まず、この実行で採番された社員だけを更新・削除する
        EmployeeTO draft = new EmployeeTO(null, "CLI sample", 10, null,
                "Sales", 300000, LocalDate.of(2021, 4, 1));
        ResponseEntity<EmployeeTO> created = client.post().uri("/employees")
                .contentType(MediaType.APPLICATION_JSON).body(draft)
                .retrieve().toEntity(EmployeeTO.class);
        int id = Objects.requireNonNull(Objects.requireNonNull(created.getBody()).employeeId());
        System.out.println("Created => " + created.getStatusCode());
        System.out.println("Location => " + created.getHeaders().getLocation());
        try {
            EmployeeTO update = new EmployeeTO(id, "CLI sample updated", 20, null,
                    "Engineer", 460000, LocalDate.of(2021, 4, 1));
            EmployeeTO updated = client.put().uri("/employees/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON).body(update)
                    .retrieve().body(EmployeeTO.class);
            System.out.println("Updated => " + updated);
        } finally {
            ResponseEntity<Void> deleted = client.delete().uri("/employees/{id}", id)
                    .retrieve().toBodilessEntity();
            System.out.println("Deleted => " + deleted.getStatusCode());
        }
        showNotFound(client, id);
    }

    // 非検出の表示
    private static void showNotFound(RestClient client, int deletedId) {
        try {
            client.get().uri("/employees/{id}", deletedId).retrieve().body(EmployeeTO.class);
            throw new IllegalStateException("削除済み社員のGETで404が返りませんでした");
        } catch (RestClientResponseException error) {
            if (error.getStatusCode().value() != 404) {
                throw error;
            }
            System.out.println("Expected error => " + error.getStatusCode());
            System.out.println("Body => " + error.getResponseBodyAsString());
        }
    }
}
