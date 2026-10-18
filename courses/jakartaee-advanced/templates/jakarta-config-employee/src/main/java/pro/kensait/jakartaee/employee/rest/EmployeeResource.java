package pro.kensait.jakartaee.employee.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/*
 * 社員を返す API（GET /api/employees、GET /api/employees/{id}）
 * 本文の JSON への変換には、JsonbConfigResolver が返す Jsonb が使われる
 */
@Path("/employees")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class EmployeeResource {
    @Inject
    EmployeeStore store;

    @GET
    public List<EmployeeTO> findAll() {
        return store.findAll();
    }

    @GET
    @Path("/{id}")
    public EmployeeTO find(@PathParam("id") int id) {
        return store.find(id).orElseThrow(() -> new NotFoundException("Employee not found: " + id));
    }
}
