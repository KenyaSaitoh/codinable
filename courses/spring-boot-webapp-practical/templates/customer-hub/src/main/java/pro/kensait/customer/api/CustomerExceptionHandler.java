package pro.kensait.customer.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import pro.kensait.customer.service.CustomerExistsException;
import pro.kensait.customer.service.CustomerNotFoundException;

/*
 * 顧客の機能を提供するクラス
 */
@RestControllerAdvice
public class CustomerExceptionHandler {

    // エンティティ非検出の処理
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFound(CustomerNotFoundException cnfx) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("customer.not-found", cnfx.getMessage()));
    }

    // エンティティ存在の処理
    @ExceptionHandler(CustomerExistsException.class)
    public ResponseEntity<?> handleEntityExists(CustomerExistsException cee) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("customer.exists", cee.getMessage()));
    }
}

