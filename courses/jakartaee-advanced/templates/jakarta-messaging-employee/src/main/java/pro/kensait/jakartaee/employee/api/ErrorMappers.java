package pro.kensait.jakartaee.employee.api;

import jakarta.jms.JMSRuntimeException;
import jakarta.transaction.TransactionalException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;
import pro.kensait.jakartaee.employee.service.SalaryBandException;

/*
 * 例外を HTTP の応答へ変換する
 */
public final class ErrorMappers {
    private ErrorMappers() {
    }

    private static Response error(Response.Status status, String message) {
        return Response.status(status).type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", String.valueOf(message))).build();
    }

    // ブローカーへ送れなかった（送信済みの可能性もあるので、送り直すときは同じ eventId で送る）
    @Provider
    public static class JMSRuntimeExceptionMapper implements ExceptionMapper<JMSRuntimeException> {
        @Override
        public Response toResponse(JMSRuntimeException e) {
            return error(Response.Status.SERVICE_UNAVAILABLE, "メッセージを送信できませんでした: " + e.getMessage());
        }
    }

    // 給与の範囲の検証に失敗した（登録と送信はロールバック済み）
    @Provider
    public static class SalaryBandExceptionMapper implements ExceptionMapper<SalaryBandException> {
        @Override
        public Response toResponse(SalaryBandException e) {
            return error(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    // @Transactional のメソッドから実行時例外が出たとき、包まれて届く場合に備えて中身で振り分ける
    @Provider
    public static class TransactionalExceptionMapper implements ExceptionMapper<TransactionalException> {
        @Override
        public Response toResponse(TransactionalException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SalaryBandException s) {
                return error(Response.Status.BAD_REQUEST, s.getMessage());
            }
            return error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
