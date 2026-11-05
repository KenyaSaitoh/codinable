package pro.kensait.spring.employee.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pro.kensait.spring.employee.entity.Employee;
import pro.kensait.spring.employee.repository.EmployeeRepository;

/*
 * 1 つの部署の集計を、@Async で非同期に実行する
 * （AOP のプロキシで実装されるので、別の Bean から呼んだときだけ非同期になる）
 */
@Service
public class DepartmentReportService {
    private static final Logger logger = LoggerFactory.getLogger(DepartmentReportService.class);

    private final EmployeeRepository employeeRepository;

    public DepartmentReportService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Async("employeeExecutor") // 【1】
    public CompletableFuture<DepartmentSummary> summarize(Integer departmentId) { // 【2】
        logger.info("[ DepartmentReportService#summarize ] departmentId={} thread={}",
                departmentId, Thread.currentThread());
        List<Employee> members = employeeRepository.findByDepartmentId(departmentId);
        return CompletableFuture.completedFuture( // 【3】
                DepartmentSummary.of(departmentId, members));
    }
}
