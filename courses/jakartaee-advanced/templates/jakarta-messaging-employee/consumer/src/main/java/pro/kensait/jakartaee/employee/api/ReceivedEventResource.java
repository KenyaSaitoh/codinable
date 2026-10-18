package pro.kensait.jakartaee.employee.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import pro.kensait.jakartaee.employee.entity.ReceivedEvent;

/*
 * 受信履歴（GET /api/events。直近 100 件）
 */
@Path("/events")
@RequestScoped
public class ReceivedEventResource {
    @PersistenceContext
    private EntityManager em;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ReceivedEvent> events() {
        return em.createQuery("SELECT r FROM ReceivedEvent r ORDER BY r.receivedAt DESC", ReceivedEvent.class)
                .setMaxResults(100)
                .getResultList();
    }
}
