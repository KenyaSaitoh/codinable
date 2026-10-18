package pro.kensait.leafbooks.external.dto;

/**
 * REST API用のエラーレスポンス
 * leaf-books-rest APIのエラーレスポンスに使用
 */
public record ErrorResponse(
        String code,
        String message
        ) {
}




