package pro.kensait.jakartaee.employee.api;

import java.net.URI;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
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

/** 確認用の社員 API（チャプター 13.1 の API を簡略化したもの。一覧は配列で返し、ETag は付けない） */
@Path("/employees")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class EmployeeApiResource {
    @Inject
    private EmployeeStore store;

    @GET
    public List<Employee> findAll() {
        return store.findAll();
    }

    @GET
    @Path("/{id}")
    public Employee find(@PathParam("id") int id) {
        return store.find(id);
    }

    @GET
    @Path("/query_by_department")
    public List<Employee> byDepartment(@QueryParam("departmentId") int departmentId) {
        return store.findAll().stream()
                .filter(employee -> Integer.valueOf(departmentId).equals(employee.departmentId()))
                .toList();
    }

    @GET
    @Path("/query_by_salary")
    public List<Employee> bySalary(@QueryParam("lowerSalary") int lowerSalary,
            @QueryParam("upperSalary") int upperSalary) {
        if (lowerSalary < 0 || lowerSalary > upperSalary) {
            throw new BadRequestException("Invalid salary range");
        }
        return store.findAll().stream().filter(employee -> employee.salary() >= lowerSalary
                && employee.salary() <= upperSalary).toList();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Valid Employee input) {
        Employee created = store.create(input);
        return Response.created(URI.create("employees/" + created.employeeId()))
                .entity(created).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Employee update(@PathParam("id") int id, @Valid Employee input) {
        return store.update(id, input);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        store.delete(id);
        return Response.noContent().build();
    }
}
