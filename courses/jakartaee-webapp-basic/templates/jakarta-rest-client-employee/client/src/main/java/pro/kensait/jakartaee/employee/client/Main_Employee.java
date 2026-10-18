package pro.kensait.jakartaee.employee.client;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Jakarta REST のクライアント API で確認用の社員 API を呼び出すコンソール
 * 社員 1 件と一覧の取得、部署・給与による検索、登録、更新、削除、削除後の 404 の確認を順に行う
 */
public class Main_Employee {

    public static void main(String[] args) {
        try (Client client = ClientBuilder.newBuilder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .build()) {
            WebTarget base = client.target(ClientOptions.baseUrl(args));
            readEmployees(base);
            searchEmployees(base);
            createUpdateDelete(base);
        }
    }

    // 社員 1 件と一覧の取得
    private static void readEmployees(WebTarget base) {
        EmployeeTO employee = base.path("employees/10001")
                .request(MediaType.APPLICATION_JSON)
                .get(EmployeeTO.class);
        System.out.println("Employee => " + employee);

        try (Response response = base.path("employees/10001")
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            System.out.println("Status => " + response.getStatus());
            System.out.println("Headers => " + response.getHeaders());
            EmployeeTO body = response.readEntity(EmployeeTO.class);
            System.out.println("Body => " + body);
        }

        List<EmployeeTO> employees = base.path("employees")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<EmployeeTO>>() { });
        System.out.println("Employees => " + employees.size() + " 件");
    }

    // 部署と給与による検索
    private static void searchEmployees(WebTarget base) {
        List<EmployeeTO> department = base.path("employees/query_by_department")
                .queryParam("departmentId", 3)
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<EmployeeTO>>() { });
        System.out.println("Department 3 => " + department.stream()
                .map(EmployeeTO::employeeName).toList());

        WebTarget salaryTarget = base.path("employees/query_by_salary")
                .queryParam("lowerSalary", 300000)
                .queryParam("upperSalary", 400000);
        List<EmployeeTO> salary = salaryTarget
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<EmployeeTO>>() { });
        System.out.println("Salary 300000-400000 => " + salary.stream()
                .map(EmployeeTO::employeeName).toList());
    }

    // 登録・更新・削除と、削除後の 404 の確認
    private static void createUpdateDelete(WebTarget base) {
        EmployeeTO draft = new EmployeeTO(null, "Walter", 3, null,
                "ASSOCIATE", 230000, LocalDate.of(2018, 4, 1));
        int id;
        try (Response created = base.path("employees")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(draft))) {
            if (created.getStatus() != 201) {
                throw new IllegalStateException("登録に失敗しました: " + created.getStatus());
            }
            System.out.println("Location => " + created.getLocation());
            id = created.readEntity(EmployeeTO.class).employeeId();
        }

        WebTarget target = base.path("employees/{id}").resolveTemplate("id", id);
        try {
            EmployeeTO update = new EmployeeTO(id, "Walter", 4, null,
                    "CHIEF", 260000, LocalDate.of(2018, 4, 1));
            EmployeeTO updated = target.request(MediaType.APPLICATION_JSON)
                    .put(Entity.json(update), EmployeeTO.class);
            System.out.println("Updated => " + updated);
        } finally {
            try (Response deleted = target.request().delete()) {
                System.out.println("Deleted => " + deleted.getStatus());
            }
        }
        confirmDeleted(base, id);
    }

    // 削除した社員を再取得し、404 だけを想定した結果として扱う（ほかの 4xx・5xx や通信失敗は伝播させる）
    private static void confirmDeleted(WebTarget base, int deletedId) {
        try {
            base.path("employees/{id}").resolveTemplate("id", deletedId)
                    .request(MediaType.APPLICATION_JSON)
                    .get(EmployeeTO.class);
            throw new IllegalStateException("削除済み社員のGETで404が返りませんでした");
        } catch (NotFoundException error) {
            Response response = error.getResponse();
            System.out.println("Expected error => " + response.getStatus());
            System.out.println("Body => " + response.readEntity(String.class));
        }
    }
}
