package pro.kensait.spring.employee.resilience.service;
import java.util.List;
/*
 * 社員結果を表すレコード
 */
public record EmployeeResult(List<EmployeeTO> employees, boolean fallback, String message) {
    // 社員結果の初期化
    public EmployeeResult {
        employees = List.copyOf(employees);
    }
}
