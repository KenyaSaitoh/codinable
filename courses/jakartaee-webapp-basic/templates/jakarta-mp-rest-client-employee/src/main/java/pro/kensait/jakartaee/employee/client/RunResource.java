package pro.kensait.jakartaee.employee.client;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/** POST /api/run で EmployeeClientRunner を動かし、実行の記録をテキストで返す */
@Path("/run")
@RequestScoped
public class RunResource {
    @Inject
    private EmployeeClientRunner runner;

    @POST
    @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
    public String run() {
        List<String> log = new ArrayList<>();
        log.addAll(runner.readEmployees());
        log.addAll(runner.searchEmployees());
        log.addAll(runner.createUpdateDelete());
        return String.join("\n", log) + "\n";
    }
}
