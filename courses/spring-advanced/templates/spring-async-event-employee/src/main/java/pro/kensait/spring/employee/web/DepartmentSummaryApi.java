package pro.kensait.spring.employee.web;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.kensait.spring.employee.service.DepartmentReportService;
import pro.kensait.spring.employee.service.DepartmentSummary;
import pro.kensait.spring.employee.service.EmployeeSummaryService;

/*
 * 部署ごとの集計を、Spring MVC の非同期応答（戻り値の CompletableFuture）で返す
 *   GET /departments/summary     … supplyAsync と allOf による並行集計
 *   GET /departments/{id}/report … @Async のメソッドの結果
 */
@RestController
@RequestMapping("/departments")
public class DepartmentSummaryApi {
    private final EmployeeSummaryService summaryService;
    private final DepartmentReportService reportService;

    public DepartmentSummaryApi(EmployeeSummaryService summaryService, DepartmentReportService reportService) {
        this.summaryService = summaryService;
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public CompletableFuture<List<DepartmentSummary>> summary() {
        return summaryService.summarizeAll()
                .orTimeout(5, TimeUnit.SECONDS);
    }

    @GetMapping("/{id}/report")
    public CompletableFuture<DepartmentSummary> report(@PathVariable("id") Integer departmentId) {
        return reportService.summarize(departmentId);
    }
}
