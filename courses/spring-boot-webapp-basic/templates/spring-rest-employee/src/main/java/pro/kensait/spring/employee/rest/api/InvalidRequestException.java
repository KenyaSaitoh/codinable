package pro.kensait.spring.employee.rest.api;
/** マスタに存在しないIDなど、入力内容の不整合 */
public final class InvalidRequestException extends RuntimeException {
    // 不正リクエストの初期化
    public InvalidRequestException(String message) {
        super(message);
    }
}
