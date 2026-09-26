package pro.kensait.customer.service;

/*
 * 顧客非検出を通知する例外
 */
public class CustomerNotFoundException extends RuntimeException {
    // 顧客非検出の初期化
    public CustomerNotFoundException() {
        super();
    }

    // 顧客非検出の初期化
    public CustomerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    // 顧客非検出の初期化
    public CustomerNotFoundException(String message) {
        super(message);
    }

    // 顧客非検出の初期化
    public CustomerNotFoundException(Throwable cause) {
        super(cause);
    }
}

