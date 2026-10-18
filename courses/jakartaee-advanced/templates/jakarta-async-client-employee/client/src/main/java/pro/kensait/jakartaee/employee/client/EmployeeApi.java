package pro.kensait.jakartaee.employee.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.concurrent.CompletionStage;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/*
 * MicroProfile Rest Client で宣言した社員APIの通信の契約
 * 実装はランタイム（Jersey）が生成する。戻り値が CompletionStage のメソッドは非同期に実行される
 */
@Path("/employees")
@RegisterRestClient(configKey = "employee-api") // 【1】
@Produces(MediaType.APPLICATION_JSON)
public interface EmployeeApi {
    @GET
    @Path("/{id}")
    CompletionStage<EmployeeTO> findAsync(@PathParam("id") int id); // 【2】

    @GET
    @Path("/query_by_department")
    CompletionStage<List<EmployeeTO>> byDepartmentAsync(
            @QueryParam("departmentId") int departmentId); // 【3】
}
