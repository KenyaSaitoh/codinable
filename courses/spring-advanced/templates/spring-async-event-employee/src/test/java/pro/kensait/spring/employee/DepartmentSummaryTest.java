package pro.kensait.spring.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pro.kensait.spring.employee.service.DepartmentReportService;
import pro.kensait.spring.employee.service.DepartmentSummary;
import pro.kensait.spring.employee.service.EmployeeSummaryService;

/*
 * 並行集計と @Async の結果を確かめる。部署なしの Victor はどの部署にも入らない
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:hsqldb:mem:departmentSummaryTest")
class DepartmentSummaryTest {
    @Autowired
    private EmployeeSummaryService summaryService;
    @Autowired
    private DepartmentReportService reportService;

    @Test
    void summarizesAllDepartmentsInOrder() {
        List<DepartmentSummary> summaries = summaryService.summarizeAll().join();
        assertEquals(List.of("PLANNING", "HR", "SALES", "PRODUCT"),
                summaries.stream().map(DepartmentSummary::departmentName).toList());
        assertEquals(List.of(3, 2, 5, 5),
                summaries.stream().map(DepartmentSummary::employeeCount).toList());
    }

    @Test
    void asyncMethodReturnsTheSameResult() {
        DepartmentSummary sales = reportService.summarize(3).join();
        assertEquals("SALES", sales.departmentName());
        assertEquals(5, sales.employeeCount());
    }
}
