package pro.kensait.spring.employee.flux.api;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
/*
 * 入力の機能を提供するクラス
 */
@RestControllerAdvice
public class InputAdvice {
    // 業務上不正な入力の場合は400の返却
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalid(IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(Map.of("message", error.getMessage()));
    }
}
