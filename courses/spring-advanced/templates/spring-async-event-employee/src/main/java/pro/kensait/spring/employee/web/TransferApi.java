package pro.kensait.spring.employee.web;

import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.kensait.spring.employee.entity.Employee;
import pro.kensait.spring.employee.entity.TransferHistory;
import pro.kensait.spring.employee.repository.TransferHistoryRepository;
import pro.kensait.spring.employee.service.EmployeeNotFoundException;
import pro.kensait.spring.employee.service.EmployeeService;

/*
 * 社員の異動（POST /transfers）と異動履歴（GET /transfers）
 * 異動の応答は、メール通知や履歴の記録を待たずに返る（それらはコミット後の非同期リスナーが行う）
 */
@RestController
@RequestMapping("/transfers")
public class TransferApi {
    private final EmployeeService employeeService;
    private final TransferHistoryRepository historyRepository;

    public TransferApi(EmployeeService employeeService, TransferHistoryRepository historyRepository) {
        this.employeeService = employeeService;
        this.historyRepository = historyRepository;
    }

    public record TransferRequest(Integer employeeId, Integer toDepartmentId) {
    }

    @PostMapping
    public Employee transfer(@RequestBody TransferRequest request) {
        return employeeService.transfer(request.employeeId(), request.toDepartmentId());
    }

    @GetMapping
    public List<TransferHistory> histories() {
        return historyRepository.findAll();
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<Map<String, String>> missing(EmployeeNotFoundException error) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", error.getMessage()));
    }

    // 異動先の部署が無いと、コミットで外部キー制約に違反してロールバックする
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> rolledBack(DataIntegrityViolationException error) {
        return ResponseEntity.badRequest().body(Map.of("message", "異動先の部署が存在しないためロールバックしました"));
    }
}
