package pro.kensait.spring.users;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/*
 * 例外を HTTP のステータスコードに対応づける
 *
 * @RestControllerAdvice に書くと、すべての @RestController に効く
 * こうしておくと Controller には正常系だけが残り、読みやすくなる
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 対象が無い → 404 Not Found */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(404, e.getMessage()));
    }

    /** 入力が決まりを満たさない → 400 Bad Request */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        // どの項目がなぜ駄目だったかを添える (クライアントが直せるように)
        Map<String, Object> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.put(fe.getField(), fe.getDefaultMessage()));

        Map<String, Object> body = body(400, "入力内容を確認してください");
        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    private Map<String, Object> body(int status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("message", message);
        return body;
    }
}
