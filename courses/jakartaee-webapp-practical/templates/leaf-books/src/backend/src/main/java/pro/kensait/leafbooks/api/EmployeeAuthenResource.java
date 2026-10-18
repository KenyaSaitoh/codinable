package pro.kensait.leafbooks.api;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import pro.kensait.leafbooks.api.dto.EmployeeLoginRequest;
import pro.kensait.leafbooks.api.dto.EmployeeLoginResponse;
import pro.kensait.leafbooks.common.ErrorResponse;
import pro.kensait.leafbooks.entity.Employee;
import pro.kensait.leafbooks.service.employee.EmployeeService;
import pro.kensait.leafbooks.security.EmployeeJwtUtil;

/**
 * 社員認証APIリソースクラス
 * パス: /admin/auth（顧客認証 /auth と区別）
 */
@Path("/admin/auth")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeAuthenResource {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeAuthenResource.class);

    @Inject
    private EmployeeService employeeService;

    @Inject
    private EmployeeJwtUtil jwtUtil;

    /**
     * 社員ログイン（社員コード + パスワード）
     * POST /api/admin/auth/login
     */
    @POST
    @Path("/login")
    public Response login(@Valid EmployeeLoginRequest request) {
        logger.info("[ EmployeeAuthenResource#login ] employeeCode: {}", request.employeeCode());

        try {
            Employee employee = employeeService.getEmployeeByCode(request.employeeCode());

            if (employee == null) {
                logger.warn("Employee not found: {}", request.employeeCode());
                return unauthorizedResponse();
            }

            String storedPassword = employee.getPassword();
            boolean passwordMatch;
            if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
                passwordMatch = BCrypt.checkpw(request.password(), storedPassword);
            } else {
                passwordMatch = request.password().equals(storedPassword);
            }

            if (!passwordMatch) {
                logger.warn("Password mismatch for employeeCode: {}", request.employeeCode());
                return unauthorizedResponse();
            }

            Long departmentId = employee.getDepartment() != null ?
                    employee.getDepartment().getDepartmentId() : null;
            String jwt = jwtUtil.generateToken(
                    employee.getEmployeeId(),
                    employee.getEmployeeCode(),
                    departmentId);

            NewCookie cookie = new NewCookie.Builder(jwtUtil.getCookieName())
                    .value(jwt)
                    .path("/")
                    .maxAge(jwtUtil.getExpirationSeconds())
                    .httpOnly(true)
                    .secure(false)
                    .build();

            String departmentName = employee.getDepartment() != null ?
                    employee.getDepartment().getDepartmentName() : null;
            EmployeeLoginResponse response = new EmployeeLoginResponse(
                    employee.getEmployeeId(),
                    employee.getEmployeeCode(),
                    employee.getEmployeeName(),
                    employee.getEmail(),
                    employee.getJobRank(),
                    departmentId,
                    departmentName);

            return Response.ok(response).cookie(cookie).build();

        } catch (Exception e) {
            logger.error("Unexpected error during employee login", e);
            ErrorResponse errorResponse = new ErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                    "Internal Server Error",
                    "ログイン処理中にエラーが発生しました",
                    "/api/admin/auth/login");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse).build();
        }
    }

    /**
     * 社員ログアウト
     * POST /api/admin/auth/logout
     */
    @POST
    @Path("/logout")
    public Response logout() {
        logger.info("[ EmployeeAuthenResource#logout ]");
        NewCookie cookie = new NewCookie.Builder(jwtUtil.getCookieName())
                .value("")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(false)
                .build();
        return Response.ok().cookie(cookie).build();
    }

    private Response unauthorizedResponse() {
        ErrorResponse errorResponse = new ErrorResponse(
                Response.Status.UNAUTHORIZED.getStatusCode(),
                "Unauthorized",
                "社員コードまたはパスワードが正しくありません",
                "/api/admin/auth/login");
        return Response.status(Response.Status.UNAUTHORIZED)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }
}
