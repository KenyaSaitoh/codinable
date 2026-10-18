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
import org.apache.kafka.clients.producer.RecordMetadata;
import pro.kensait.jakartaee.employee.service.EmployeeEvent;
import pro.kensait.jakartaee.employee.service.EventSendService;

/*
 * 社員イベントの送信（POST /api/events）
 * send が戻ったら 202（ブローカーが記録を受け付けた）。応答にはパーティションとオフセットを入れる
 */
@Path("/events")
@RequestScoped
public class EventResource {
    @Inject
    private EventSendService eventSendService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response send(EmployeeEvent request) {
        EmployeeEvent event = request.withDefaults();
        RecordMetadata metadata = eventSendService.send(event);
        return Response.accepted(Map.of("eventId", event.eventId(), "topic", metadata.topic(),
                "partition", metadata.partition(), "offset", metadata.offset())).build();
    }
}
