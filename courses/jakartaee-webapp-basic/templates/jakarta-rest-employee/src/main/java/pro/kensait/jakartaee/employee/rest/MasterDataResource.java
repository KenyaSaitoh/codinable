package pro.kensait.jakartaee.employee.rest;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import pro.kensait.jakartaee.employee.entity.Department;
import pro.kensait.jakartaee.employee.entity.Job;
import pro.kensait.jakartaee.employee.service.EmployeeService;

/** 入力の選択に使うマスター（GET /api/departments、GET /api/jobs） */
@Path("/")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class MasterDataResource {
    @Inject
    private EmployeeService employeeService;

    @GET
    @Path("/departments")
    public List<Department> departments() {
        return employeeService.departments();
    }

    @GET
    @Path("/jobs")
    public List<Job> jobs() {
        return employeeService.jobs();
    }
}
