package pro.kensait.jakartaee.employee.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import pro.kensait.jakartaee.employee.entity.Employee;
import pro.kensait.jakartaee.employee.service.EmployeeRegistrationService;

/*
 * 社員の登録（POST /api/employees）
 * 登録と社員イベントの送信が同じトランザクションで確定する。給与が範囲外なら 400 で、どちらも取り消される
 */
@Path("/employees")
@RequestScoped
public class EmployeeResource {
    @Inject
    private EmployeeRegistrationService registrationService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(Employee employee, @Context UriInfo uriInfo) {
        Employee created = registrationService.register(employee);
        return Response.created(uriInfo.getAbsolutePathBuilder()
                        .path(String.valueOf(created.getEmployeeId())).build())
                .entity(created).build();
    }
}
