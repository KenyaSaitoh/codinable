package pro.kensait.spring.employee.web;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.kensait.spring.employee.service.Employee;
import pro.kensait.spring.employee.service.EmployeeService;

/*
 * アクセストークンで保護した社員 API（/employees）
 * どの権限で呼べるかは SecurityConfig に書いてある
 */
@RestController
@RequestMapping("/employees")
public class EmployeeApi {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeApi.class);
    private final EmployeeService service;

    public EmployeeApi(EmployeeService service) {
        this.service = service;
    }

    @GetMapping
    public List<Employee> employees() {
        return service.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id, @AuthenticationPrincipal Jwt jwt) { // 【1】
        service.delete(id);
        logger.info("社員を削除しました id={} by={} sub={}", id,
                jwt.getClaimAsString("preferred_username"), jwt.getSubject()); // 【2】
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> missing(NoSuchElementException error) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", error.getMessage()));
    }
}
