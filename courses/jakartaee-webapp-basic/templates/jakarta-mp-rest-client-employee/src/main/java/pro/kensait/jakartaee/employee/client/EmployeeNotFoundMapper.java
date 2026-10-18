package pro.kensait.jakartaee.employee.client;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

public class EmployeeNotFoundMapper
        implements ResponseExceptionMapper<EmployeeNotFoundException> {

    @Override
    public boolean handles(int status, MultivaluedMap<String, Object> headers) {
        return status == 404;
    }

    @Override
    public EmployeeNotFoundException toThrowable(Response response) {
        return new EmployeeNotFoundException(response.readEntity(String.class));
    }
}
