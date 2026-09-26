package pro.kensait.spring.employee.web;
import java.net.URI;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pro.kensait.spring.employee.service.Employee;
import pro.kensait.spring.employee.service.EmployeeService;
/*
 * 社員管理システムのREST APIを表すクラス
 */
@RestController
public class EmployeeApi {
    private final EmployeeService service;
    // 社員APIの初期化
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "Spring owns this shared service; the controller does not expose or own its state")
    public EmployeeApi(EmployeeService service) {
        this.service = service;
    }
    // csrfの実行
    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }
    // セッションの実行
    @GetMapping("/session")
    public Map<String, Object> session(Authentication user) {
        return Map.of("name", user.getName(), "authorities",
                user.getAuthorities().stream().map(Object::toString).toList());
    }
    // クエリメソッド：全Employeeリスト取得
    @GetMapping("/employees")
    public List<Employee> employees() {
        return service.findAll();
    }
    // 引数は@Argumentによりスキーマの引数「id」からバインドされる）
    @GetMapping("/employees/{id}")
    public Employee employee(@PathVariable int id) {
        return service.find(id);
    }
    // 社員の登録
    @PostMapping("/employees")
    public ResponseEntity<Employee> create(@Valid @RequestBody Employee input) {
        Employee result = service.save(null, input);
        return ResponseEntity.created(URI.create("/employees/" + result.employeeId())).body(result);
    }
    // 指定した社員の更新
    @PutMapping("/employees/{id}")
    public Employee update(@PathVariable int id, @Valid @RequestBody Employee input) {
        return service.save(id, input);
    }
    // APIメソッド：Employeeの削除
    @DeleteMapping("/employees/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
    // 未存在の実行
    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> missing(java.util.NoSuchElementException error) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", error.getMessage()));
    }
}
