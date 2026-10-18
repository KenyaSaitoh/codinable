package pro.kensait.jakartaee.employee.client;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

/**
 * MicroProfile Rest Client で確認用の社員 API を呼び出す
 * 社員 1 件と一覧の取得、部署・給与による検索、登録、更新、削除、削除後の 404 の確認を順に行い、記録を返す
 */
@ApplicationScoped
public class EmployeeClientRunner {
    @Inject
    @RestClient
    private EmployeeApiClient client;

    public List<String> readEmployees() {
        List<String> log = new ArrayList<>();
        log.add("Employee => " + client.find(10001));
        log.add("Employees => " + client.findAll());
        log.add("Salary => " + client.bySalary(300000, 400000));
        return log;
    }

    public List<String> searchEmployees() {
        List<String> log = new ArrayList<>();
        log.add("Department 3 => " + client.byDepartment(3).stream()
                .map(EmployeeTO::employeeName).toList());
        return log;
    }

    public List<String> createUpdateDelete() {
        List<String> log = new ArrayList<>();
        EmployeeTO draft = new EmployeeTO(null, "Walter", 3, null,
                "ASSOCIATE", 230000, LocalDate.of(2018, 4, 1));
        int id;
        try (Response created = client.create(draft)) {
            if (created.getStatus() != 201) {
                throw new IllegalStateException("登録に失敗しました: " + created.getStatus());
            }
            log.add("Location => " + created.getLocation());
            id = created.readEntity(EmployeeTO.class).employeeId();
        }
        try {
            EmployeeTO update = new EmployeeTO(id, "Walter", 4, null,
                    "CHIEF", 260000, LocalDate.of(2018, 4, 1));
            log.add("Updated => " + client.update(id, update));
        } finally {
            client.delete(id);
            log.add("Deleted => " + id);
        }
        // 削除後の 404 だけを想定した結果として扱う（ほかの 4xx・5xx や通信失敗は伝播させる）
        try {
            client.find(id);
            throw new IllegalStateException("削除済み社員のGETで404が返りませんでした");
        } catch (EmployeeNotFoundException expected) {
            log.add("Expected error => 404 " + expected.getMessage());
        }
        return log;
    }
}
