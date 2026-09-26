package pro.kensait.spring.calc.service.exception;

/*
 * 極度オーバー時に発生する例外クラス
 */
public class LimitOverException extends Exception {

    // 上限超過の初期化
    public LimitOverException() {
        super();
    }

    // 上限超過の初期化
    public LimitOverException(String message, Throwable cause) {
        super(message, cause);
    }

    // 上限超過の初期化
    public LimitOverException(String message) {
        super(message);
    }

    // 上限超過の初期化
    public LimitOverException(Throwable cause) {
        super(cause);
    }
}