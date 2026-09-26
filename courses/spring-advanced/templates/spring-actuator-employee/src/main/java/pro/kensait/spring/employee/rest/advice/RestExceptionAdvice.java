package pro.kensait.spring.employee.rest.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import pro.kensait.spring.employee.rest.service.EmployeeNotFoundException;

/*
 * REST APIで発生した例外を横断的に処理するアドバイスクラス
 * （@RestControllerAdviceにより、すべての@RestControllerに適用される）
 */
@RestControllerAdvice
public class RestExceptionAdvice {
    private static final Logger logger = LoggerFactory.getLogger(
            RestExceptionAdvice.class);

    // 例外ハンドラ：社員が存在しない場合はステータス404で応答
    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeNotFound(
            EmployeeNotFoundException ex) {
        logger.info("[ RestExceptionAdvice#handleEmployeeNotFound ]");

        // ステータスが404でボディにエラー情報（JSON）を保持するResponseEntityを生成し、返す
        ErrorResponse body = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // 例外ハンドラ：データ整合性違反（外部キー制約違反など）の場合はステータス409で応答
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {
        logger.info("[ RestExceptionAdvice#handleDataIntegrityViolation ]");

        // ステータスが409でボディにエラー情報（JSON）を保持するResponseEntityを生成し、返す
        ErrorResponse body = new ErrorResponse(
                HttpStatus.CONFLICT.value(), "Conflict",
                "データ整合性違反が発生しました");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // 例外ハンドラ：その他の例外の場合はステータス500で応答
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        logger.info("[ RestExceptionAdvice#handleException ]");

        // ステータスが500でボディにエラー情報（JSON）を保持するResponseEntityを生成し、返す
        ErrorResponse body = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
                "予期しないエラーが発生しました");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // エラー情報（JSONレスポンス）を表すレコード
    record ErrorResponse(
            int status,     // HTTPステータスコード
            String error,   // エラー種別
            String message  // エラーメッセージ
            ) {
    }
}
