package pro.kensait.spring.employee.api;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pro.kensait.spring.employee.entity.Employee;
import pro.kensait.spring.employee.entity.EmployeeSearchCriteria;
import pro.kensait.spring.employee.service.EmployeePage;
import pro.kensait.spring.employee.service.EmployeeService;

/*
 * 社員情報を操作するREST API
 */
@RestController
@RequestMapping("/employees")
public class EmployeeApi {
    @Autowired
    private EmployeeService employeeService;

    // 条件に一致する社員の検索
    @GetMapping
    public EmployeePage search(@ModelAttribute EmployeeSearchCriteria criteria,
            @RequestParam(defaultValue = "1") int page) {
        return employeeService.search(criteria.normalized(), page);
    }

    // 指定した社員の取得
    @GetMapping("/{employeeId}")
    public Employee get(@PathVariable Integer employeeId) {
        return employeeService.get(employeeId);
    }

    // 社員の登録
    @PostMapping
    public ResponseEntity<Employee> create(@Valid @RequestBody EmployeeRequest request) {
        validateReferences(request);
        Employee created = employeeService.create(request.toEmployee());
        return ResponseEntity.created(URI.create("/employees/" + created.getEmployeeId()))
                .body(created);
    }

    // 指定した社員の更新
    @PutMapping("/{employeeId}")
    public Employee update(@PathVariable Integer employeeId,
            @Valid @RequestBody EmployeeRequest request) {
        validateReferences(request);
        if (request.version() == null) {
            throw new InvalidRequestException("更新時は取得したversionを指定してください");
        }
        return employeeService.update(employeeId, request.toEmployee(), request.version());
    }

    // 指定した社員の削除
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<Void> delete(@PathVariable Integer employeeId) {
        employeeService.delete(employeeId);
        return ResponseEntity.noContent().build();
    }

    // 既存のREST教材で使っていた月給下限検索も引き続き提供
    @GetMapping("/query_by_salary")
    public List<Employee> queryByLowerSalary(@RequestParam Integer lowerSalary) {
        if (lowerSalary < 0 || lowerSalary > 9_999_999) {
            throw new InvalidRequestException("月給は0以上9999999以下で指定してください");
        }
        return employeeService.salaryAtLeast(lowerSalary);
    }

    // 指定された部署と役職が存在することの確認
    private void validateReferences(EmployeeRequest request) {
        if (!employeeService.departmentExists(request.departmentId())) {
            throw new InvalidRequestException("指定した部署が存在しません");
        }
        if (!employeeService.jobExists(request.jobId())) {
            throw new InvalidRequestException("指定した役職が存在しません");
        }
    }
}
