package pro.kensait.jakartaee.employee.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pro.kensait.jakartaee.employee.service.EmployeeService;
import pro.kensait.jakartaee.employee.service.EmployeeTO;

/*
 * 社員API（GET /api/employees、GET /api/employees/{employeeId}）
 */
@Path("/employees")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class EmployeeResource {
    @Inject
    private EmployeeService employeeService;

    @GET
    public List<EmployeeTO> employees() {
        return employeeService.getEmployeesAll();
    }

    @GET
    @Path("/{employeeId}")
    public EmployeeTO employee(@PathParam("employeeId") Integer employeeId) {
        return employeeService.getEmployee(employeeId);
    }
}
