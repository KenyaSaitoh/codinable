package pro.kensait.leafbooks.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import pro.kensait.leafbooks.api.dto.ErrorResponse;

/**
 * 403 Forbidden例外マッパー
 * 認証済みだが、そのリソースを操作する権限がない場合に使う
 */
@Provider
public class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {
    private static final Logger logger = LoggerFactory.getLogger(ForbiddenExceptionMapper.class);

    @Override
    public Response toResponse(ForbiddenException exception) {
        logger.warn("[ ForbiddenExceptionMapper ] Forbidden: {}", exception.getMessage());

        ErrorResponse errorResponse = ErrorResponse.forbidden(
                exception.getMessage() != null ? exception.getMessage() : "この操作は許可されていません",
                "");

        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }
}
