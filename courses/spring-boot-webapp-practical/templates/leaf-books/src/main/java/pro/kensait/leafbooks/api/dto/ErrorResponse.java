package pro.kensait.leafbooks.api.dto;

import java.time.LocalDateTime;
import java.util.List;

/*
 * エラー情報（JSONレスポンス）を表すレコード
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
    // エラーの初期化
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }

    // エラーの初期化
    public ErrorResponse(int status, String error, String message, String path, List<String> details) {
        this(LocalDateTime.now(), status, error, message, path, details);
    }
}

