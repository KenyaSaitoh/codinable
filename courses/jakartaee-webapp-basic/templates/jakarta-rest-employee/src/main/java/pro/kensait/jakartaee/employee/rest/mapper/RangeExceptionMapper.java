package pro.kensait.jakartaee.employee.rest.mapper;

import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pro.kensait.jakartaee.employee.rest.ApiError;
import pro.kensait.jakartaee.employee.service.RangeException;

@Provider
public class RangeExceptionMapper implements ExceptionMapper<RangeException> {
    @Override
    public Response toResponse(RangeException exception) {
        ApiError error = new ApiError(400, exception.getMessage(), List.of());
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(error).build();
    }
}
