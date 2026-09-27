package pro.kensait.spring.employee.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import pro.kensait.spring.employee.client.Main_Employee;
/*
 * 社員APIのテスト
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmployeeApiTest {
    @LocalServerPort
    private int port;
    private RestClient client;

    // テスト前処理
    @BeforeEach
    void connect() {
        client = RestClient.create("http://127.0.0.1:" + port);
    }

    // 「初期社員を変更しないコンソールクライアントの再実行」の検証
    @Test
    void consoleClientCanRunTwiceWithoutChangingInitialEmployees() throws Exception {
        List<Employee> before = list("/employees");
        assertEquals(16, before.size());
        Main_Employee.main(new String[] {"http://127.0.0.1:" + port});
        Main_Employee.main(new String[] {"http://127.0.0.1:" + port});
        assertEquals(before, list("/employees"));
    }

    // 「サーバー採番IDを使うCRUDとHTTP応答」の検証
    @Test
    void crudUsesServerIdsAndReturnsExpectedStatusAndLocation() {
        Employee original = client.get().uri("/employees/10001").retrieve().body(Employee.class);
        ResponseEntity<Employee> response = client.post().uri("/employees")
                .body(input("Walter", 3, 230000)).retrieve().toEntity(Employee.class);
        assertEquals(201, response.getStatusCode().value());
        int id = response.getBody().employeeId();
        assertTrue(id >= 10017);
        assertEquals("/employees/" + id, response.getHeaders().getLocation().toString());
        try {
            Employee updated = client.put().uri("/employees/{id}", id)
                    .body(input("Walter", 1, 250000)).retrieve().body(Employee.class);
            assertEquals(id, updated.employeeId());
            assertEquals("PLANNING", updated.departmentName());
            assertEquals(250000, updated.salary());
            assertEquals(updated, client.get().uri("/employees/{id}", id)
                    .retrieve().body(Employee.class));
            assertEquals(original, client.get().uri("/employees/10001").retrieve().body(Employee.class));
        } finally {
            assertEquals(204, client.delete().uri("/employees/{id}", id)
                    .retrieve().toBodilessEntity().getStatusCode().value());
        }
        assertEquals(404, assertThrows(RestClientResponseException.class,
                () -> client.get().uri("/employees/{id}", id).retrieve().body(Employee.class))
                .getStatusCode().value());
    }

    // 「部署と境界値を含む月給範囲による検索」の検証
    @Test
    void searchesByDepartmentAndInclusiveSalaryRange() {
        assertEquals(List.of(10001, 10004, 10005, 10010, 10013),
                list("/employees/query_by_department?departmentId=3")
                .stream().map(Employee::employeeId).toList());
        assertEquals(List.of(10003, 10004, 10005, 10011, 10015, 10016), list(
                "/employees/query_by_salary?lowerSalary=300000&upperSalary=400000")
                .stream().map(Employee::employeeId).toList());
        assertTrue(list("/employees/query_by_department?departmentId=99").isEmpty());
        assertEquals(400, assertThrows(RestClientResponseException.class, () -> list(
                "/employees/query_by_salary?lowerSalary=400000&upperSalary=300000"))
                .getStatusCode().value());
    }

    // 「保存済み社員を変更しない不正入力の拒否」の検証
    @Test
    void rejectsInvalidInputWithoutChangingStoredEmployees() {
        List<Employee> before = list("/employees");
        for (Employee invalid : List.of(input(" ", 3, 230000), input("Walter", 99, 230000),
                input("Walter", 3, -1), input("Walter", null, 230000), input("Walter", 3, null))) {
            assertEquals(400, assertThrows(RestClientResponseException.class,
                    () -> client.post().uri("/employees").body(invalid)
                            .retrieve().body(Employee.class)).getStatusCode().value());
        }
        assertEquals(before, list("/employees"));
    }

    // 「未存在社員の更新・削除の禁止」の検証
    @Test
    void missingEmployeeCannotBeUpdatedOrDeleted() {
        assertEquals(404, assertThrows(RestClientResponseException.class,
                () -> client.put().uri("/employees/999999").body(input("Walter", 3, 230000))
                        .retrieve().body(Employee.class)).getStatusCode().value());
        assertEquals(404, assertThrows(RestClientResponseException.class,
                () -> client.delete().uri("/employees/999999").retrieve().toBodilessEntity())
                .getStatusCode().value());
    }

    // 入力の実行
    private Employee input(String name, Integer departmentId, Integer salary) {
        // 入力のIDや部署名で既存データを上書きできないことも確認する
        return new Employee(10001, name, departmentId, "Untrusted department", "ASSOCIATE", salary,
                LocalDate.of(2018, 4, 1));
    }

    // データ一覧の取得
    private List<Employee> list(String uri) {
        return client.get().uri(uri).retrieve()
                .body(new ParameterizedTypeReference<List<Employee>>() {});
    }
}
