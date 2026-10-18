package pro.kensait.jakartaee.employee.rest.mapper;

import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pro.kensait.jakartaee.employee.rest.ApiError;
import pro.kensait.jakartaee.employee.service.ConflictException;

@Provider
public class ConflictExceptionMapper implements ExceptionMapper<ConflictException> {
    @Override
    public Response toResponse(ConflictException exception) {
        ApiError error = new ApiError(409, exception.getMessage(), List.of());
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(error).build();
    }
}
