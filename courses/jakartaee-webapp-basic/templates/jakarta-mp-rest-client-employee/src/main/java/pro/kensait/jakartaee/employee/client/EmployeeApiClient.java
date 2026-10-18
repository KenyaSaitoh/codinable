package pro.kensait.jakartaee.employee.client;

import java.util.List;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** 確認用の社員 API の通信契約。実装クラスは書かず、実行環境が生成する */
@RegisterRestClient(configKey = "employee-api")
@RegisterProvider(EmployeeNotFoundMapper.class)
@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
public interface EmployeeApiClient {
    @GET
    List<EmployeeTO> findAll();

    @GET
    @Path("/{id}")
    EmployeeTO find(@PathParam("id") int id);

    @GET
    @Path("/query_by_department")
    List<EmployeeTO> byDepartment(@QueryParam("departmentId") int departmentId);

    @GET
    @Path("/query_by_salary")
    List<EmployeeTO> bySalary(@QueryParam("lowerSalary") int lowerSalary,
            @QueryParam("upperSalary") int upperSalary);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response create(EmployeeTO draft);

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    EmployeeTO update(@PathParam("id") int id, EmployeeTO input);

    @DELETE
    @Path("/{id}")
    void delete(@PathParam("id") int id);
}
