package pro.kensait.customer.service;

/*
 * 顧客存在を通知する例外
 */
public class CustomerExistsException extends RuntimeException {
    // 顧客存在の初期化
    public CustomerExistsException() {
        super();
    }

    // 顧客存在の初期化
    public CustomerExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    // 顧客存在の初期化
    public CustomerExistsException(String message) {
        super(message);
    }

    // 顧客存在の初期化
    public CustomerExistsException(Throwable cause) {
        super(cause);
    }
}

