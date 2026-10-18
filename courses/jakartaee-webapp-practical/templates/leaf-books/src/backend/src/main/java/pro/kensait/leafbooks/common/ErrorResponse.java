package pro.kensait.leafbooks.common;

import java.util.List;

/**
 * エラーレスポンスDTO
 */
public record ErrorResponse(
    Integer status,
    String error,
    String message,
    String path,
    List<String> details
) {
    // 詳細なしのコンストラクタ
    public ErrorResponse(Integer status, String error, String message, String path) {
        this(status, error, message, path, null);
    }

    // 401（未認証）
    public static ErrorResponse unauthorized(String message, String path) {
        return new ErrorResponse(401, "Unauthorized", message, path);
    }

    // 403（権限なし）
    public static ErrorResponse forbidden(String message, String path) {
        return new ErrorResponse(403, "Forbidden", message, path);
    }
}

