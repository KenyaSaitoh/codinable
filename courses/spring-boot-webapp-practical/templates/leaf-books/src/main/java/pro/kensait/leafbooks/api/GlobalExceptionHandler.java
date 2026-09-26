package pro.kensait.leafbooks.api;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.persistence.OptimisticLockException;
import pro.kensait.leafbooks.api.dto.ErrorResponse;
import pro.kensait.leafbooks.external.CustomerExistsException;
import pro.kensait.leafbooks.external.CustomerNotFoundException;
import pro.kensait.leafbooks.service.order.OutOfStockException;

/*
 * globalの機能を提供するクラス
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // バリデーションエラー
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        logger.warn("[ GlobalExceptionHandler ] Validation error: {}", ex.getMessage());

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "入力値に誤りがあります",
                request.getDescription(false).replace("uri=", ""),
                errors
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    // 顧客が存在しない
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFoundException(
            CustomerNotFoundException ex, WebRequest request) {
        logger.warn("[ GlobalExceptionHandler ] Customer not found: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "指定された顧客が見つかりません",
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    // 顧客が既に存在
    @ExceptionHandler(CustomerExistsException.class)
    public ResponseEntity<ErrorResponse> handleCustomerExistsException(
            CustomerExistsException ex, WebRequest request) {
        logger.warn("[ GlobalExceptionHandler ] Customer already exists: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                "指定されたメールアドレスは既に登録されています",
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    // 在庫不足
    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<ErrorResponse> handleOutOfStockException(
            OutOfStockException ex, WebRequest request) {
        logger.warn("[ GlobalExceptionHandler ] Out of stock: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Out of Stock",
                "「" + ex.getBookName() + "」の在庫が不足しています",
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    // 楽観的ロックエラー
    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockException(
            OptimisticLockException ex, WebRequest request) {
        logger.warn("[ GlobalExceptionHandler ] Optimistic lock error: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                "他のユーザーによって更新されています再度お試しください",
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    // その他の例外
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        logger.error("[ GlobalExceptionHandler ] Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "予期しないエラーが発生しました",
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

