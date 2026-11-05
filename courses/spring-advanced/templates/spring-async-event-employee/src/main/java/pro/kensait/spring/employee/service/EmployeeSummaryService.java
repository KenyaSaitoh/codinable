package pro.kensait.spring.employee.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import pro.kensait.spring.employee.entity.Employee;
import pro.kensait.spring.employee.repository.EmployeeRepository;

/*
 * 4 つの部署の社員数と平均月給を、部署ごとに並行して集計する
 */
@Service
public class EmployeeSummaryService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeSummaryService.class);
    private static final List<Integer> DEPARTMENT_IDS = List.of(1, 2, 3, 4);

    private final Executor executor;
    private final EmployeeRepository employeeRepository;

    public EmployeeSummaryService(@Qualifier("employeeExecutor") Executor executor,
            EmployeeRepository employeeRepository) {
        this.executor = executor;
        this.employeeRepository = employeeRepository;
    }

    public CompletableFuture<List<DepartmentSummary>> summarizeAll() {
        List<CompletableFuture<DepartmentSummary>> futures = DEPARTMENT_IDS.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> summarize(id), executor)) // 【1】
                .toList();
        return CompletableFuture
                .allOf(futures.toArray(CompletableFuture[]::new)) // 【2】
                .thenApply(done -> futures.stream()
                        .map(CompletableFuture::join) // 【3】
                        .toList());
    }

    private DepartmentSummary summarize(Integer departmentId) {
        // どのスレッドで動いたかを出す（仮想スレッドなら VirtualThread と表示される）
        logger.info("[ EmployeeSummaryService#summarize ] departmentId={} thread={}",
                departmentId, Thread.currentThread());
        List<Employee> members = employeeRepository.findByDepartmentId(departmentId);
        return DepartmentSummary.of(departmentId, members);
    }
}
