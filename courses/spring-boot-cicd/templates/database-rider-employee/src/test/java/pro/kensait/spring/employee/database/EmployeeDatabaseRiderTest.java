package pro.kensait.spring.employee.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import javax.sql.DataSource;
import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.api.DBRider;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import pro.kensait.spring.employee.Application;
import pro.kensait.spring.employee.api.EmployeeRequest;
import pro.kensait.spring.employee.entity.Employee;
import pro.kensait.spring.employee.service.EmployeePage;
/** APIを呼び、コミット後のDBをYAMLと比較するテストを@Transactionalにしない */
@DBRider
@DBUnit(cacheConnection = false)
@Tag("it")
@SpringBootTest(classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EmployeeDatabaseRiderTest {
    @Autowired private DataSource dataSource;
    @LocalServerPort private int port;
    private final ConnectionHolder connectionHolder = () -> dataSource.getConnection();

    // クライアントの実行
    private RestClient client() {
        return RestClient.create("http://localhost:" + port);
    }

    // 「YAMLからの社員データ読み込み」の検証
    @Test
    @DataSet(value = "datasets/employees.yml", cleanBefore = true, cleanAfter = true)
    void loadsEmployeesFromYaml() {
        EmployeePage found = client().get().uri("/employees?departmentId=1&salaryFrom=300000")
                .retrieve().body(EmployeePage.class);
        assertThat(found.content()).extracting(Employee::getEmployeeName).containsExactly("Alice");
        assertThat(found.totalElements()).isEqualTo(1);
    }

    // 「更新後のデータベース状態」の検証
    @Test
    @DataSet(value = "datasets/employees.yml", cleanBefore = true, cleanAfter = true)
    @ExpectedDataSet("datasets/employees-updated.yml")
    void verifiesUpdatedDatabaseState() {
        Employee updated = client().put().uri("/employees/101")
                .body(input("Alice", 2, 330000, 0)).retrieve().body(Employee.class);
        assertThat(updated.getVersion()).isEqualTo(1);
    }

    // 「論理削除後のデータベース状態」の検証
    @Test
    @DataSet(value = "datasets/employees.yml", cleanBefore = true, cleanAfter = true)
    @ExpectedDataSet("datasets/employees-deleted.yml")
    void verifiesLogicallyDeletedDatabaseState() {
        var response = client().delete().uri("/employees/101").retrieve().toBodilessEntity();
        assertThat(response.getStatusCode().value()).isEqualTo(204);
        assertThatThrownBy(() -> client().get().uri("/employees/101").retrieve().body(Employee.class))
                .isInstanceOfSatisfying(RestClientResponseException.class,
                        error -> assertThat(error.getStatusCode().value()).isEqualTo(404));
    }

    // 「拒否された更新によるデータベースの不変性」の検証
    @Test
    @DataSet(value = "datasets/employees.yml", cleanBefore = true, cleanAfter = true)
    @ExpectedDataSet("datasets/employees.yml")
    void rejectedUpdateLeavesDatabaseUnchanged() {
        assertThatThrownBy(() -> client().put().uri("/employees/101")
                .body(input("Bob", 2, 330000, 99)).retrieve().body(Employee.class))
                .isInstanceOfSatisfying(RestClientResponseException.class,
                        error -> assertThat(error.getStatusCode().value()).isEqualTo(409));
    }

    // 「登録後のデータベース状態」の検証
    @Test
    @DataSet(value = "datasets/employees.yml", cleanBefore = true, cleanAfter = true,
            executeStatementsBefore = "ALTER TABLE EMPLOYEE ALTER COLUMN EMPLOYEE_ID RESTART WITH 103")
    @ExpectedDataSet("datasets/employees-created.yml")
    void verifiesCreatedDatabaseState() {
        var response = client().post().uri("/employees").body(input("Carol", 1, 300000, null))
                .retrieve().toEntity(Employee.class);
        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getHeaders().getLocation()).hasToString("/employees/103");
    }

    // 「不正な部署による社員の未登録」の検証
    @Test
    @DataSet(value = "datasets/employees.yml", cleanBefore = true, cleanAfter = true)
    @ExpectedDataSet("datasets/employees.yml")
    void invalidDepartmentDoesNotInsertEmployee() {
        assertThatThrownBy(() -> client().post().uri("/employees")
                .body(input("Dave", 999, 300000, null)).retrieve().body(Employee.class))
                .isInstanceOfSatisfying(RestClientResponseException.class,
                        error -> assertThat(error.getStatusCode().value()).isEqualTo(400));
    }

    // 入力の実行
    private EmployeeRequest input(String name, int departmentId, int salary, Integer version) {
        return new EmployeeRequest(name, departmentId, 1, salary, LocalDate.of(2020, 4, 1), version);
    }
}
