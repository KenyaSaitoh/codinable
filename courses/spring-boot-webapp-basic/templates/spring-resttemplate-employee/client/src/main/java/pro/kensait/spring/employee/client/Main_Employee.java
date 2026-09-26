package pro.kensait.spring.employee.client;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
/*
 * main社員の機能を提供するクラス
 */
public class Main_Employee {
    public static void main(String[] args) {
        String baseUrl = ClientOptions.baseUrl(args);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));
        RestTemplate client = new RestTemplate(factory);
        readEmployees(client, baseUrl);
        searchEmployees(client, baseUrl);
        createUpdateDelete(client, baseUrl);
    }

    // 社員の取得
    private static void readEmployees(RestTemplate client, String baseUrl) {
        // getForObject()はDTO、getForEntity()はステータス・ヘッダー付きの応答を返す
        EmployeeTO employee = client.getForObject(baseUrl + "/employees/1", EmployeeTO.class);
        System.out.println("Employee => " + employee);
        ResponseEntity<EmployeeTO> response = client.getForEntity(
                baseUrl + "/employees/1", EmployeeTO.class);
        System.out.println("Status => " + response.getStatusCode());
        System.out.println("Headers => " + response.getHeaders());
        EmployeeTO[] array = client.getForObject(baseUrl + "/employees", EmployeeTO[].class);
        System.out.println("Array => " + Arrays.asList(Objects.requireNonNull(array)));
        // ParameterizedTypeReferenceでList<EmployeeTO>の型情報を指定する
        ResponseEntity<List<EmployeeTO>> list = client.exchange(baseUrl + "/employees",
                HttpMethod.GET, null, new ParameterizedTypeReference<List<EmployeeTO>>() { });
        System.out.println("List => " + list.getBody());
    }

    // 社員の検索
    private static void searchEmployees(RestTemplate client, String baseUrl) {
        String departmentUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/employees/query_by_department").queryParam("departmentId", 10).toUriString();
        ResponseEntity<List<EmployeeTO>> department = client.exchange(departmentUrl,
                HttpMethod.GET, null, new ParameterizedTypeReference<List<EmployeeTO>>() { });
        System.out.println("Department => " + department.getBody());
        String salaryUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/employees/query_by_salary")
                .queryParam("lowerSalary", 300000).queryParam("upperSalary", 400000).toUriString();
        ResponseEntity<List<EmployeeTO>> salary = client.exchange(salaryUrl,
                HttpMethod.GET, null, new ParameterizedTypeReference<List<EmployeeTO>>() { });
        System.out.println("Salary => " + salary.getBody());
    }

    // updatedeleteの生成
    private static void createUpdateDelete(RestTemplate client, String baseUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        EmployeeTO draft = new EmployeeTO(null, "CLI sample", 10, null,
                "Sales", 300000, LocalDate.of(2021, 4, 1));
        ResponseEntity<EmployeeTO> created = client.postForEntity(baseUrl + "/employees",
                new HttpEntity<>(draft, headers), EmployeeTO.class);
        int id = Objects.requireNonNull(Objects.requireNonNull(created.getBody()).employeeId());
        System.out.println("Created => " + created.getStatusCode());
        System.out.println("Location => " + created.getHeaders().getLocation());
        try {
            EmployeeTO update = new EmployeeTO(id, "CLI sample updated", 20, null,
                    "Engineer", 460000, LocalDate.of(2021, 4, 1));
            client.put(baseUrl + "/employees/{id}", new HttpEntity<>(update, headers), id);
            System.out.println("Updated => "
                    + client.getForObject(baseUrl + "/employees/{id}", EmployeeTO.class, id));
        } finally {
            // delete()は戻り値なし204を調べたい場合はexchange(..., DELETE, ..., Void.class)
            client.delete(baseUrl + "/employees/{id}", id);
            System.out.println("Deleted => " + id);
        }
        showNotFound(client, baseUrl, id);
    }

    // 非検出の表示
    private static void showNotFound(RestTemplate client, String baseUrl, int deletedId) {
        try {
            client.getForEntity(baseUrl + "/employees/{id}", EmployeeTO.class, deletedId);
            throw new IllegalStateException("削除済み社員のGETで404が返りませんでした");
        } catch (HttpClientErrorException error) {
            if (error.getStatusCode().value() != 404) {
                throw error;
            }
            System.out.println("Expected error => " + error.getStatusCode());
            System.out.println("Body => " + error.getResponseBodyAsString());
        }
    }
}
