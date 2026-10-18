package pro.kensait.jakartaee.employee.api;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import pro.kensait.jakartaee.employee.entity.Employee;
import pro.kensait.jakartaee.employee.event.TransferBroadcaster;
import pro.kensait.jakartaee.employee.repository.EmployeeRepository;

/*
 * 社員の一覧（GET /api/employees）と、SSE による配信
 *   GET /api/employees/stream … 社員 16 件を約 1 秒間隔で送り、最後に complete を送って閉じる（有限の配信）
 *   GET /api/employees/events … 異動が起きるたびに transferred を送る（SseBroadcaster による一斉配信）
 */
@Path("/employees")
@RequestScoped
public class EmployeeStreamResource {
    private static final Logger logger = Logger.getLogger(EmployeeStreamResource.class.getName());

    @Resource(lookup = "java:app/concurrent/EmployeeExecutor")
    private ManagedExecutorService executor;
    @Inject
    private EmployeeRepository employeeRepository;
    @Inject
    private TransferBroadcaster broadcaster;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<EmployeeView> employees() {
        return employeeRepository.findAllOrderById().stream().map(EmployeeView::from).toList();
    }

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS) // 【1】
    public void stream(@Context SseEventSink sink, @Context Sse sse) { // 【2】
        List<Employee> employees = employeeRepository.findAllOrderById();
        executor.runAsync(() -> { // 【3】
            try (sink) {
                for (Employee employee : employees) {
                    if (sink.isClosed()) { // 【4】
                        return;
                    }
                    sink.send(sse.newEventBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_TYPE)
                            .data(EmployeeView.class, EmployeeView.from(employee)) // 【5】
                            .build()).toCompletableFuture().join();
                    Thread.sleep(1000);
                }
                sink.send(sse.newEvent("complete", "{\"complete\":true}")) // 【6】
                        .toCompletableFuture().join();
            } catch (IOException | InterruptedException e) {
                logger.log(Level.INFO, "配信を終了しました", e);
            }
        });
    }

    @GET
    @Path("/events")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void events(@Context SseEventSink sink, @Context Sse sse) {
        broadcaster.register(sink, sse);
    }
}
