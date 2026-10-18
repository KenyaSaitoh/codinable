package pro.kensait.jakartaee.employee.api;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;
import pro.kensait.jakartaee.employee.service.EventSendException;

/*
 * Kafka へ送れなかったときは 503（記録された可能性もあるので、送り直すときは同じ eventId で送る）
 */
@Provider
public class EventSendExceptionMapper implements ExceptionMapper<EventSendException> {
    @Override
    public Response toResponse(EventSendException e) {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE).type(MediaType.APPLICATION_JSON)
                .entity(Map.of("error", e.getMessage())).build();
    }
}
