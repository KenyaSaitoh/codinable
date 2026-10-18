package pro.kensait.jakartaee.employee.rest.mapper;

import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pro.kensait.jakartaee.employee.rest.ApiError;
import pro.kensait.jakartaee.employee.service.EmployeeNotFoundException;

@Provider
public class EmployeeNotFoundExceptionMapper implements ExceptionMapper<EmployeeNotFoundException> {
    @Override
    public Response toResponse(EmployeeNotFoundException exception) {
        ApiError error = new ApiError(404, exception.getMessage(), List.of());
        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(error).build();
    }
}
