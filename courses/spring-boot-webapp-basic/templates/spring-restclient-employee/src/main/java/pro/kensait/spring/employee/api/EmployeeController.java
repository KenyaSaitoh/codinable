package pro.kensait.spring.employee.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/*
 * 社員APIのコントローラー
 */
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    // この確認用APIだけが保持するメモリ内データ
    private final EmployeeStore store = new EmployeeStore();

    // 全件検索
    @GetMapping
    public List<Employee> findAll() {
        return store.findAll();
    }

    // 主キー検索
    @GetMapping("/{id}")
    public Employee find(@PathVariable int id) {
        return store.find(id);
    }

    // 部署による検索
    @GetMapping("/query_by_department")
    public List<Employee> byDepartment(@RequestParam int departmentId) {
        return store.findAll().stream()
                .filter(employee -> Integer.valueOf(departmentId).equals(employee.departmentId()))
                .toList();
    }

    // 月給の範囲による検索
    @GetMapping("/query_by_salary")
    public List<Employee> bySalary(@RequestParam int lowerSalary, @RequestParam int upperSalary) {
        if (lowerSalary < 0 || lowerSalary > upperSalary) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid salary range");
        }
        return store.findAll().stream().filter(employee -> employee.salary() >= lowerSalary
                && employee.salary() <= upperSalary).toList();
    }

    // 社員の新規登録
    @PostMapping
    public ResponseEntity<Employee> create(@Valid @RequestBody Employee input) {
        Employee created = store.create(input);
        return ResponseEntity.created(URI.create("/employees/" + created.employeeId())).body(created);
    }

    // 社員の更新
    @PutMapping("/{id}")
    public Employee update(@PathVariable int id, @Valid @RequestBody Employee input) {
        return store.update(id, input);
    }

    // APIメソッド：Employeeの削除
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        store.delete(id);
        return ResponseEntity.noContent().build();
    }
}
