package pro.kensait.jakartaee.employee.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;

/*
 * 確認用の社員API（http://localhost:8080/jakarta-async-client-employee/api/employees）
 * client/ の Java コンソールが、非同期の HTTP クライアントでここを呼ぶ
 * 存在しない社員は 404、登録は 201 と Location ヘッダー、削除は 204 を返す
 */
@Path("/employees")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeResource {
    @Inject
    EmployeeStore store;

    // 全件の取得
    @GET
    public List<EmployeeTO> findAll() {
        return store.findAll();
    }

    // 社員1件の取得
    @GET
    @Path("/{id}")
    public EmployeeTO find(@PathParam("id") int id) {
        return store.find(id);
    }

    // 部署による検索（該当者がいなければ空の配列）
    @GET
    @Path("/query_by_department")
    public List<EmployeeTO> byDepartment(@QueryParam("departmentId") int departmentId) {
        return store.findAll().stream()
                .filter(employee -> Integer.valueOf(departmentId).equals(employee.departmentId()))
                .toList();
    }

    // 給与の範囲による検索
    @GET
    @Path("/query_by_salary")
    public List<EmployeeTO> bySalary(@QueryParam("lowerSalary") int lowerSalary,
            @QueryParam("upperSalary") int upperSalary) {
        if (lowerSalary < 0 || lowerSalary > upperSalary) {
            throw new BadRequestException("Invalid salary range");
        }
        return store.findAll().stream()
                .filter(employee -> employee.salary() >= lowerSalary && employee.salary() <= upperSalary)
                .toList();
    }

    // 登録（201 と Location ヘッダー）
    @POST
    public Response create(@Valid EmployeeTO input, @Context UriInfo uriInfo) {
        EmployeeTO created = store.create(input);
        return Response.created(uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(created.employeeId())).build())
                .entity(created).build();
    }

    // 更新
    @PUT
    @Path("/{id}")
    public EmployeeTO update(@PathParam("id") int id, @Valid EmployeeTO input) {
        return store.update(id, input);
    }

    // 削除（204）
    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id) {
        store.delete(id);
        return Response.noContent().build();
    }
}
