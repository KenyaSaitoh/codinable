package pro.kensait.jakartaee.employee.web;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import pro.kensait.jakartaee.employee.service.Employee;
import pro.kensait.jakartaee.employee.service.EmployeeService;

/*
 * 社員API（/api/employees）
 * 認可の基本は web.xml の security-constraint。@RolesAllowed はメソッドごとの意図を示す二重の確認
 */
@Path("/employees")
@RequestScoped
public class EmployeeResource {
    @Inject
    private EmployeeService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"BASIC", "ADMIN"}) // 【1】
    public List<Employee> employees() {
        return service.findAll();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"BASIC", "ADMIN"})
    public Employee employee(@PathParam("id") int id) {
        return service.find(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("ADMIN") // 【2】
    public Response create(@Valid Employee input, @Context UriInfo uriInfo) {
        Employee result = service.save(null, input);
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(result.employeeId())).build(); // 【3】
        return Response.created(location).entity(result).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("ADMIN")
    public Employee update(@PathParam("id") int id, @Valid Employee input) {
        return service.save(id, input);
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response delete(@PathParam("id") int id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
