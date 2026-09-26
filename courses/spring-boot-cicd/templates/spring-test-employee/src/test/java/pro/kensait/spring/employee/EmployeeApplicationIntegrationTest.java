package pro.kensait.spring.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pro.kensait.spring.employee.entity.Employee;
import pro.kensait.spring.employee.entity.EmployeeSearchCriteria;
import pro.kensait.spring.employee.repository.EmployeeRepository;
import pro.kensait.spring.employee.service.ConflictException;
import pro.kensait.spring.employee.service.EmployeeService;
import pro.kensait.spring.employee.service.NotFoundException;

/*
 * 社員アプリケーションのテスト
 */
@Tag("it")
@SpringBootTest(classes = Application.class)
@Transactional
class EmployeeApplicationIntegrationTest {
    @Autowired private EmployeeService service;
    @Autowired private EmployeeRepository repository;

    // 入力の実行
    private Employee input(String name) {
        Employee employee = new Employee();
        employee.setEmployeeName(name);
        employee.setDepartmentId(1);
        employee.setJobId(2);
        employee.setSalary(300000);
        employee.setEntranceDate(LocalDate.of(2026, 4, 1));
        return employee;
    }

    // 「参照マスターデータの読み込み」の検証
    @Test
    void loadsReferenceMasterData() {
        assertThat(service.departments()).hasSize(4);
        assertThat(service.jobs()).hasSize(5);
        assertThat(service.search(EmployeeSearchCriteria.empty(), 1).totalElements()).isEqualTo(10);
    }

    // 「社員の登録・更新・論理削除」の検証
    @Test
    void createsUpdatesAndLogicallyDeletesEmployee() {
        Employee created = service.create(input("Alice"));
        int id = created.getEmployeeId();
        assertThat(created.getEmployeeCode()).isEqualTo(String.format("E%04d", id));
        Employee updated = service.update(id, input("Bob"), 0);
        assertThat(service.get(id).getEmployeeName()).isEqualTo("Bob");
        assertThat(updated.getVersion()).isEqualTo(1);
        service.delete(id);
        assertThatThrownBy(() -> service.get(id)).isInstanceOf(NotFoundException.class);
        assertThat(repository.findById(id).orElseThrow().getStatus()).isEqualTo(Employee.DELETED);
        assertThat(service.search(EmployeeSearchCriteria.empty(), 1).totalElements()).isEqualTo(10);
    }

    // 「古い更新による保存済み値の上書き防止」の検証
    @Test
    void staleUpdateDoesNotOverwriteSavedValues() {
        Employee created = service.create(input("Alice"));
        int id = created.getEmployeeId();
        service.update(id, input("Bob"), 0);
        assertThatThrownBy(() -> service.update(id, input("Carol"), 0))
                .isInstanceOf(ConflictException.class);
        assertThat(service.get(id).getEmployeeName()).isEqualTo("Bob");
        assertThat(service.get(id).getVersion()).isEqualTo(1);
    }
}
