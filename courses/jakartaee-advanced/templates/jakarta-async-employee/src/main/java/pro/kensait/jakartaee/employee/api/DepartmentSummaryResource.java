package pro.kensait.jakartaee.employee.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import pro.kensait.jakartaee.employee.service.DepartmentReportService;
import pro.kensait.jakartaee.employee.service.DepartmentSummary;
import pro.kensait.jakartaee.employee.service.EmployeeSummaryService;

/*
 * 部署ごとの集計を、Jakarta REST の非同期応答で返す
 *   GET /api/departments/summary       … @Suspended AsyncResponse
 *   GET /api/departments/summary-stage … 戻り値の CompletionStage
 *   GET /api/departments/{id}/report   … @Asynchronous のメソッドの結果
 */
@Path("/departments")
@RequestScoped
public class DepartmentSummaryResource {
    @Inject
    private EmployeeSummaryService summaryService;
    @Inject
    private DepartmentReportService reportService;

    @GET
    @Path("/summary")
    @Produces(MediaType.APPLICATION_JSON)
    public void summary(@Suspended AsyncResponse response) { // 【1】
        response.setTimeout(5, TimeUnit.SECONDS); // 【2】
        summaryService.summarizeAll()
                .thenAccept(response::resume) // 【3】
                .exceptionally(error -> {
                    response.resume(error);
                    return null;
                });
    }

    @GET
    @Path("/summary-stage")
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<List<DepartmentSummary>> summaryStage() { // 【4】
        return summaryService.summarizeAll() // 【5】
                .orTimeout(5, TimeUnit.SECONDS);
    }

    @GET
    @Path("/{id}/report")
    @Produces(MediaType.APPLICATION_JSON)
    public CompletionStage<DepartmentSummary> report(@PathParam("id") Integer departmentId) {
        return reportService.summarize(departmentId);
    }
}
