package pro.kensait.jakartaee.employee.rest;

import java.net.URI;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import pro.kensait.jakartaee.employee.entity.Department;
import pro.kensait.jakartaee.employee.entity.Employee;
import pro.kensait.jakartaee.employee.entity.Job;
import pro.kensait.jakartaee.employee.service.EmployeePage;
import pro.kensait.jakartaee.employee.service.EmployeeService;

/** 社員管理 API のリソースクラス。URL・HTTP メソッド・入力 DTO・応答の型をここで宣言する */
@Path("/employees")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class EmployeeResource {
    @Inject
    private EmployeeService employeeService;

    @Context
    private Request httpRequest;

    @Context
    private UriInfo uriInfo;

    @GET
    public EmployeePage search(@BeanParam EmployeeSearchParam param,
            @QueryParam("page") @DefaultValue("1") int page) {
        return employeeService.search(param.toCriteria(), page);
    }

    @GET
    @Path("/query_by_salary")
    public List<EmployeeResponse> queryBySalary(
            @QueryParam("lowerSalary") @DefaultValue("0") int lowerSalary) {
        return employeeService.findByLowerSalary(lowerSalary).stream()
                .map(EmployeeResponse::from).toList();
    }

    @GET
    @Path("/{employeeId}")
    public Response get(@PathParam("employeeId") Integer employeeId) {
        Employee employee = employeeService.get(employeeId);
        EntityTag tag = tagOf(employee);
        Response.ResponseBuilder notModified = httpRequest.evaluatePreconditions(tag);
        if (notModified != null) {
            return notModified.build();
        }
        return Response.ok(EmployeeResponse.from(employee)).tag(tag).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Valid EmployeeRequest request) {
        Employee created = employeeService.create(toEmployee(request));
        return Response.created(locationOf(created))
                .entity(EmployeeResponse.from(created))
                .tag(tagOf(created)).build();
    }

    @PUT
    @Path("/{employeeId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@PathParam("employeeId") Integer employeeId,
            @Valid EmployeeRequest request) {
        Employee input = toEmployee(request);
        Response.ResponseBuilder failed =
                httpRequest.evaluatePreconditions(tagOf(employeeService.get(employeeId)));
        if (failed != null) {
            return failed.build();
        }
        if (request.version() == null) {
            throw new InvalidRequestException("更新時は取得したversionを指定してください");
        }
        Employee updated = employeeService.update(
                employeeId, input, request.version());
        return Response.ok(EmployeeResponse.from(updated)).tag(tagOf(updated)).build();
    }

    // 論理削除（status を deleted にする）。本文なしの 204
    @DELETE
    @Path("/{employeeId}")
    public Response delete(@PathParam("employeeId") Integer employeeId) {
        employeeService.delete(employeeId);
        return Response.noContent().build();
    }

    private Employee toEmployee(EmployeeRequest request) {
        Department department = employeeService.findDepartment(request.departmentId())
                .orElseThrow(() -> new InvalidRequestException("指定した部署が存在しません"));
        Job job = employeeService.findJob(request.jobId())
                .orElseThrow(() -> new InvalidRequestException("指定した役職が存在しません"));
        return request.toEmployee(department, job);
    }

    private EntityTag tagOf(Employee employee) {
        return new EntityTag(String.valueOf(employee.getVersion()));
    }

    private URI locationOf(Employee created) {
        return uriInfo.getBaseUriBuilder()
                .path(EmployeeResource.class)
                .path(EmployeeResource.class, "get")
                .build(created.getEmployeeId());
    }
}
