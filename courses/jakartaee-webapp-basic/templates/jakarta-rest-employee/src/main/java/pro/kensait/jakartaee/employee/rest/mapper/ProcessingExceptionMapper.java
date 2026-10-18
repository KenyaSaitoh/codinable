package pro.kensait.jakartaee.employee.rest.mapper;

import java.util.List;

import jakarta.json.JsonException;
import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pro.kensait.jakartaee.employee.rest.ApiError;

/**
 * 本文の JSON を読み取れなかった（壊れた JSON、型の合わない値）ことを 400 にする
 * Payara の Jakarta REST 実装は、JSON Binding の変換の失敗を ProcessingException に包んで送出する
 */
@Provider
public class ProcessingExceptionMapper implements ExceptionMapper<ProcessingException> {
    @Override
    public Response toResponse(ProcessingException exception) {
        if (isJsonError(exception)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ApiError(400, "本文のJSONを読み取れません", List.of())).build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ApiError(500, "サーバーでエラーが発生しました", List.of())).build();
    }

    private boolean isJsonError(Throwable exception) {
        for (Throwable t = exception; t != null; t = t.getCause()) {
            if (t instanceof JsonbException || t instanceof JsonException) {
                return true;
            }
        }
        return false;
    }
}
