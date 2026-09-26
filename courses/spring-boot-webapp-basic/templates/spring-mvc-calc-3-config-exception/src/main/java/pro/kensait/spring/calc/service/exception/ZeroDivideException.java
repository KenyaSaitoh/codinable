package pro.kensait.spring.calc.service.exception;

/*
 * ゼロ割り時に発生する例外クラス
 */
public class ZeroDivideException extends Exception {

    // ゼロdivideの初期化
    public ZeroDivideException() {
        super();
    }

    // ゼロdivideの初期化
    public ZeroDivideException(String message, Throwable cause) {
        super(message, cause);
    }

    // ゼロdivideの初期化
    public ZeroDivideException(String message) {
        super(message);
    }

    // ゼロdivideの初期化
    public ZeroDivideException(Throwable cause) {
        super(cause);
    }
}
