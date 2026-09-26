package pro.kensait.spring.employee.api;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import pro.kensait.spring.employee.service.ConflictException;
import pro.kensait.spring.employee.service.NotFoundException;
import pro.kensait.spring.employee.service.RangeException;

/*
 * REST APIで発生した例外をエラーレスポンスに変換するクラス
 */
@RestControllerAdvice
public class ApiExceptionHandler {
    /*
     * APIのエラーレスポンス
     */
    public record ApiError(int status, String message, List<String> errors) {
        // APIエラーの初期化
        public ApiError {
            errors = List.copyOf(errors);
        }

        // 呼び出し元によるエラー一覧の変更の防止
        @Override
        public List<String> errors() {
            return List.copyOf(errors);
        }
    }

    // 対象データが存在しない場合は404の返却
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    // 同時更新を検出した場合は409の返却
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflict(ConflictException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    // 業務上不正な入力の場合は400の返却
    @ExceptionHandler({InvalidRequestException.class, RangeException.class})
    public ResponseEntity<ApiError> invalid(RuntimeException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    // 入力値検証のエラーを項目ごとのメッセージへの変換
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> validation(BindException exception) {
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(field -> field.getField() + ": " + field.getDefaultMessage()).toList();
        return ResponseEntity.badRequest().body(new ApiError(400, "入力内容を確認してください", errors));
    }

    // JSONやリクエストパラメータの形式が不正な場合は400の返却
    @ExceptionHandler({HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiError> malformed() {
        return error(HttpStatus.BAD_REQUEST, "JSONの項目・値、またはパラメータの形式を確認してください");
    }

    // データの整合性制約に違反した場合は409の返却
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integrity() {
        return error(HttpStatus.CONFLICT, "関連データが変更されました読み込み直してください");
    }

    // ステータスとメッセージから共通形式のエラーレスポンスの生成
    private ResponseEntity<ApiError> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiError(status.value(), message, List.of()));
    }
}
