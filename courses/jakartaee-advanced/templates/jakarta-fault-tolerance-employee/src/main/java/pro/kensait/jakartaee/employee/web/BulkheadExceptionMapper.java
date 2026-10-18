package pro.kensait.jakartaee.employee.web;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.faulttolerance.exceptions.BulkheadException;

/*
 * 同時実行数の上限による拒否を、500 ではなく 503 と Retry-After で返す
 */
@Provider
public class BulkheadExceptionMapper
        implements ExceptionMapper<BulkheadException> { // 【1】

    @Override
    public Response toResponse(BulkheadException ex) {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE) // 【2】
                .header("Retry-After", "1") // 【3】
                .type(MediaType.TEXT_PLAIN_TYPE)
                .entity("同時実行数の上限により呼び出しを制限しました")
                .build();
    }
}
