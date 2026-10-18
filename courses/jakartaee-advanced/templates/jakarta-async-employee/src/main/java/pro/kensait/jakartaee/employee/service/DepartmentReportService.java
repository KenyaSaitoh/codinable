package pro.kensait.jakartaee.employee.service;

import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import pro.kensait.jakartaee.employee.entity.Employee;
import pro.kensait.jakartaee.employee.repository.EmployeeRepository;

/*
 * 1 つの部署の集計を、@Asynchronous で非同期に実行する
 * （インターセプタで実装されるので、別の CDI Bean から呼んだときだけ非同期になる）
 */
@ApplicationScoped
public class DepartmentReportService {

    @Inject
    private EmployeeRepository employeeRepository;

    @Asynchronous(executor = "java:app/concurrent/EmployeeExecutor") // 【1】
    public CompletableFuture<DepartmentSummary> summarize(Integer departmentId) { // 【2】
        List<Employee> members = employeeRepository.findByDepartmentId(departmentId);
        return Asynchronous.Result.complete( // 【3】
                DepartmentSummary.of(departmentId, members));
    }
}
