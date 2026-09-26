package pro.kensait.spring.employee.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import pro.kensait.spring.employee.entity.Employee;
import pro.kensait.spring.employee.entity.EmployeeSearchCriteria;
import pro.kensait.spring.employee.repository.DepartmentRepository;
import pro.kensait.spring.employee.repository.EmployeeRepository;
import pro.kensait.spring.employee.repository.JobRepository;

/*
 * 社員のテスト
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock private EmployeeRepository employees;
    @Mock private DepartmentRepository departments;
    @Mock private JobRepository jobs;
    @InjectMocks private EmployeeService service;

    // 社員の実行
    private Employee employee() {
        Employee employee = new Employee();
        employee.setEmployeeId(11);
        employee.setEmployeeName("Alice");
        employee.setDepartmentId(1);
        employee.setJobId(2);
        employee.setSalary(300000);
        employee.setEntranceDate(LocalDate.of(2026, 4, 1));
        return employee;
    }

    // 「指定ページの安定した並び順による検索」の検証
    @Test
    void searchesRequestedPageWithStableOrder() {
        var criteria = EmployeeSearchCriteria.empty();
        var pageable = PageRequest.of(1, 5, Sort.by("employeeId"));
        when(employees.search(criteria, pageable)).thenReturn(new PageImpl<>(List.of(employee()), pageable, 6));
        EmployeePage result = service.search(criteria, 2);
        assertThat(result.content()).extracting(Employee::getEmployeeId).containsExactly(11);
        assertThat(result.totalPages()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(6);
    }

    // 「不正なページ番号の先頭ページへの補正」の検証
    @Test
    void clampsPageNumberToFirstPage() {
        var criteria = EmployeeSearchCriteria.empty();
        when(employees.search(eq(criteria), any())).thenReturn(new PageImpl<>(List.of()));
        service.search(criteria, 0);
        verify(employees).search(criteria, PageRequest.of(0, 5, Sort.by("employeeId")));
    }

    // 「検索前の逆転した月給範囲の拒否」の検証
    @Test
    void rejectsReversedSalaryRangeBeforeQuerying() {
        var criteria = EmployeeSearchCriteria.empty();
        criteria.setSalaryFrom(400000);
        criteria.setSalaryTo(300000);
        assertThatThrownBy(() -> service.search(criteria, 1)).isInstanceOf(RangeException.class);
        verify(employees, never()).search(any(), any());
    }

    // 「登録時の社員コードと初期状態の設定」の検証
    @Test
    void assignsCodeAndInitialStateWhenCreating() {
        Employee input = employee();
        input.setEmployeeId(null);
        when(employees.save(input)).thenAnswer(invocation -> {
            input.setEmployeeId(11);
            return input;
        });
        var created = service.create(input);
        assertThat(created.getEmployeeCode()).isEqualTo("E0011");
        assertThat(created.getStatus()).isEqualTo(Employee.ACTIVE);
        assertThat(created.getVersion()).isZero();
    }

    // 「項目更新とバージョン加算」の検証
    @Test
    void updatesFieldsAndIncrementsVersion() {
        Employee stored = employee();
        when(employees.findById(11)).thenReturn(Optional.of(stored));
        when(employees.save(stored)).thenReturn(stored);
        Employee input = employee();
        input.setEmployeeName("Bob");
        input.setSalary(400000);
        var updated = service.update(11, input, 0);
        assertThat(updated.getEmployeeName()).isEqualTo("Bob");
        assertThat(updated.getSalary()).isEqualTo(400000);
        assertThat(updated.getVersion()).isEqualTo(1);
    }

    // 「古い・未指定バージョンの未保存」の検証
    @Test
    void rejectsStaleOrMissingVersionWithoutSaving() {
        Employee stored = employee();
        stored.setVersion(1);
        when(employees.findById(11)).thenReturn(Optional.of(stored));
        assertThatThrownBy(() -> service.update(11, employee(), 0)).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> service.update(11, employee(), null)).isInstanceOf(ConflictException.class);
        verify(employees, never()).save(any());
    }

    // 「社員の論理削除」の検証
    @Test
    void logicallyDeletesEmployee() {
        Employee stored = employee();
        when(employees.findById(11)).thenReturn(Optional.of(stored));
        service.delete(11);
        assertThat(stored.getStatus()).isEqualTo(Employee.DELETED);
        verify(employees).save(stored);
        verify(employees, never()).deleteById(any());
    }

    // 「未存在・削除済み社員の未検出扱い」の検証
    @Test
    void treatsMissingAndDeletedEmployeesAsNotFound() {
        assertThatThrownBy(() -> service.get(999)).isInstanceOf(NotFoundException.class);
        Employee deleted = employee();
        deleted.setStatus(Employee.DELETED);
        when(employees.findById(11)).thenReturn(Optional.of(deleted));
        assertThatThrownBy(() -> service.get(11)).isInstanceOf(NotFoundException.class);
    }
}
