package pro.kensait.spring.employee.api;

/*
 * リクエストが業務上の入力条件を満たさない場合に発生する例外
 */
public final class InvalidRequestException extends RuntimeException {
    // 不正リクエストの初期化
    public InvalidRequestException(String message) {
        super(message);
    }
}
