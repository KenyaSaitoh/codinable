package pro.kensait.jakartaee.employee.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/*
 * 別プロセスの社員API（api/、http://localhost:8095）を呼ぶ MicroProfile Rest Client
 * 接続先とタイムアウトは microprofile-config.properties の employee-api/mp-rest/*
 */
@RegisterRestClient(configKey = "employee-api") // 【1】
@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
public interface EmployeeApiClient {

    @GET
    List<EmployeeTO> findAll(); // 【2】

    @GET
    @Path("/{employeeId}")
    EmployeeTO find(@PathParam("employeeId") Integer employeeId); // 【3】
}
