package pro.kensait.customer.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * エラー情報（JSONレスポンス）を表すレコード
 */
public record ErrorResponse(
        @JsonProperty(value = "code")
        String code,

        @JsonProperty(value = "message")
        String message
        ) {
}

