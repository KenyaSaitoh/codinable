package pro.kensait.spring.employee.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import pro.kensait.spring.employee.entity.Employee;
import pro.kensait.spring.employee.entity.EmployeeSearchCriteria;

/*
 * 社員のテスト
 */
@Tag("it")
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
class EmployeeRepositoryTest {
    @Autowired private EmployeeRepository repository;

    // 「在籍社員のID順ページング」の検証
    @Test
    void pagesActiveEmployeesInIdOrder() {
        var criteria = EmployeeSearchCriteria.empty();
        var first = repository.search(criteria, PageRequest.of(0, 5, Sort.by("employeeId")));
        var second = repository.search(criteria, PageRequest.of(1, 5, Sort.by("employeeId")));
        assertThat(first.getTotalElements()).isEqualTo(16);
        assertThat(first.getContent()).extracting(Employee::getEmployeeId).containsExactly(10001, 10002, 10003, 10004, 10005);
        assertThat(second.getContent()).extracting(Employee::getEmployeeId).containsExactly(10006, 10007, 10008, 10009, 10010);
    }

    // 「キーワード・部署・役職・月給境界値の複合検索」の検証
    @Test
    void combinesKeywordDepartmentJobAndInclusiveSalaryBounds() {
        var criteria = EmployeeSearchCriteria.empty();
        criteria.setKeyword("Bob");
        criteria.setDepartmentId(1);
        criteria.setJobId(4);
        criteria.setSalaryFrom(450000);
        criteria.setSalaryTo(450000);
        assertThat(repository.search(criteria, PageRequest.of(0, 5)).getContent())
                .extracting(Employee::getEmployeeId).containsExactly(10002);
        criteria.setSalaryFrom(450001);
        assertThat(repository.search(criteria, PageRequest.of(0, 5))).isEmpty();
    }

    // 「論理削除済み社員の除外」の検証
    @Test
    void excludesLogicallyDeletedEmployees() {
        Employee employee = repository.findById(10001).orElseThrow();
        employee.setStatus(Employee.DELETED);
        repository.saveAndFlush(employee);
        var found = repository.search(EmployeeSearchCriteria.empty(), PageRequest.of(0, 20));
        assertThat(found.getTotalElements()).isEqualTo(15);
        assertThat(found).extracting(Employee::getEmployeeId).doesNotContain(10001);
        assertThat(repository.findById(10001)).isPresent();
    }

    // 「未存在部署に対するデータベース制約」の検証
    @Test
    void databaseRejectsUnknownDepartment() {
        Employee employee = repository.findById(10001).orElseThrow();
        employee.setDepartmentId(999);
        assertThatThrownBy(() -> repository.saveAndFlush(employee))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // 「未存在役職に対するデータベース制約」の検証
    @Test
    void databaseRejectsUnknownJob() {
        Employee employee = repository.findById(10001).orElseThrow();
        employee.setJobId(999);
        assertThatThrownBy(() -> repository.saveAndFlush(employee))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
