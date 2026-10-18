package pro.kensait.jakartaee.employee.web;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import pro.kensait.jakartaee.employee.service.Employee;
import pro.kensait.jakartaee.employee.service.EmployeeService;

/*
 * アクセストークンで保護した社員API（/api/employees）
 * Cookie を使わないので、CSRF トークンは使わない
 */
@Path("/employees")
@RequestScoped
public class EmployeeResource {
    private static final Logger logger = Logger.getLogger(EmployeeResource.class.getName());
    @Inject
    private JsonWebToken jwt; // 【1】
    @Inject
    @Claim(standard = Claims.preferred_username)
    private String userName; // 【2】
    @Inject
    private EmployeeService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"BASIC", "ADMIN"}) // 【3】
    public List<Employee> employees() {
        return service.findAll();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN") // 【4】
    public Response delete(@PathParam("id") int id) {
        service.delete(id);
        logger.info(() -> "社員を削除しました id=" + id + " by=" + userName
                + " sub=" + jwt.getSubject()); // 【5】
        return Response.noContent().build();
    }
}
