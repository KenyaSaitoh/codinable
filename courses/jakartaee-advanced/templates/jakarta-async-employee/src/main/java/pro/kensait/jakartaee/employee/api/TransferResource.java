package pro.kensait.jakartaee.employee.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pro.kensait.jakartaee.employee.entity.TransferHistory;
import pro.kensait.jakartaee.employee.repository.TransferHistoryRepository;
import pro.kensait.jakartaee.employee.service.EmployeeService;

/*
 * 社員の異動（POST /api/transfers）と異動履歴（GET /api/transfers）
 * 異動の応答は、メール通知や履歴の記録を待たずに返る（それらはコミット後の非同期オブザーバーが行う）
 */
@Path("/transfers")
@RequestScoped
public class TransferResource {
    @Inject
    private EmployeeService employeeService;
    @Inject
    private TransferHistoryRepository historyRepository;

    public record TransferRequest(Integer employeeId, Integer toDepartmentId) {
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public EmployeeView transfer(TransferRequest request) {
        return EmployeeView.from(employeeService.transfer(request.employeeId(), request.toDepartmentId()));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<TransferHistory> histories() {
        return historyRepository.findAll();
    }
}
