package pro.kensait.jakartaee.employee.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import pro.kensait.jakartaee.employee.service.EmployeeEvent;
import pro.kensait.jakartaee.employee.service.EventSendService;
import pro.kensait.jakartaee.employee.service.PoisonMessageSender;

/*
 * 社員イベントの送信（POST /api/events）
 * send が戻ったら 202（ブローカーが受け付けた）。受信側の処理が終わったことは意味しない
 * ブローカーへ送れなかったときの JMSRuntimeException は、JMSRuntimeExceptionMapper が 503 にする
 */
@Path("/events")
@RequestScoped
public class EventResource {
    @Inject
    private EventSendService eventSendService;
    @Inject
    private PoisonMessageSender poisonMessageSender;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response send(EmployeeEvent request) {
        EmployeeEvent event = request.withDefaults();
        eventSendService.send(event);
        return Response.accepted(Map.of("eventId", event.eventId())).build();
    }

    // 教材用: JSON として読めない本文を送る（受信側で再配送され、上限を超えるとデッドレターキューへ移る）
    @POST
    @Path("/raw")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response sendRaw(String text) {
        poisonMessageSender.send(text);
        return Response.accepted().build();
    }
}
