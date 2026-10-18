package pro.kensait.jakartaee.employee.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.concurrent.CompletionStage;
import pro.kensait.jakartaee.employee.client.EmployeeTO;
import pro.kensait.jakartaee.employee.service.EmployeeClientService;
import pro.kensait.jakartaee.employee.service.EmployeeResult;

/*
 * Fault Tolerance の確認用の入口（/jakarta-fault-tolerance-employee/api/...）
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class ResilienceResource {

    @Inject
    private EmployeeClientService employeeClientService; // 【1】

    @GET
    @Path("/resilience-demo")
    public EmployeeResult resilienceDemo() {
        return employeeClientService.getEmployees(); // 【2】
    }

    @GET
    @Path("/bulkhead-demo")
    public EmployeeTO bulkheadDemo() {
        return employeeClientService.getEmployee(10001); // 【3】
    }

    @GET
    @Path("/bulkhead-async-demo")
    public CompletionStage<EmployeeTO> bulkheadAsyncDemo() {
        return employeeClientService.getEmployeeAsync(10001);
    }

    @GET
    @Path("/retry-demo")
    public EmployeeResult retryDemo() {
        return employeeClientService.getEmployeesWithRetry(); // 【4】
    }
}
