package pro.kensait.jakartaee.employee.service;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import pro.kensait.jakartaee.employee.entity.Employee;
import pro.kensait.jakartaee.employee.repository.EmployeeRepository;

/*
 * 4 つの部署の社員数と平均月給を、部署ごとに並行して集計する
 */
@ApplicationScoped
public class EmployeeSummaryService {
    private static final Logger logger = Logger.getLogger(EmployeeSummaryService.class.getName());
    private static final List<Integer> DEPARTMENT_IDS = List.of(1, 2, 3, 4);

    @Resource(lookup = "java:app/concurrent/EmployeeExecutor") // 【1】
    private ManagedExecutorService executor;

    @Inject
    private EmployeeRepository employeeRepository;

    public CompletableFuture<List<DepartmentSummary>> summarizeAll() {
        List<CompletableFuture<DepartmentSummary>> futures = DEPARTMENT_IDS.stream()
                .map(id -> executor.supplyAsync(() -> summarize(id))) // 【2】
                .toList();
        return CompletableFuture
                .allOf(futures.toArray(CompletableFuture[]::new)) // 【3】
                .thenApply(done -> futures.stream()
                        .map(CompletableFuture::join) // 【4】
                        .toList());
    }

    private DepartmentSummary summarize(Integer departmentId) {
        // どのスレッドで動いたかを出す（仮想スレッドなら VirtualThread と表示される）
        logger.info("[ EmployeeSummaryService#summarize ] departmentId=" + departmentId
                + " thread=" + Thread.currentThread());
        List<Employee> members = employeeRepository.findByDepartmentId(departmentId);
        return DepartmentSummary.of(departmentId, members); // 【5】
    }
}
