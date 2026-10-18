package pro.kensait.jakartaee.employee.web;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;
import pro.kensait.jakartaee.employee.service.EmployeeNotFoundException;

/*
 * 存在しない社員を 404 にする
 */
@Provider
public class EmployeeNotFoundExceptionMapper implements ExceptionMapper<EmployeeNotFoundException> {
    @Override
    public Response toResponse(EmployeeNotFoundException e) {
        return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", e.getMessage())).build();
    }
}
