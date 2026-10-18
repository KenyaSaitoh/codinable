package pro.kensait.jakartaee.employee.event;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseBroadcaster;
import jakarta.ws.rs.sse.SseEventSink;

/*
 * 異動を、購読中のすべての SSE の接続へ一斉配信する（3 つ目の非同期オブザーバー）
 * EmployeeService も TransferEventRelay も変更せずに通知先を追加できている
 */
@ApplicationScoped
public class TransferBroadcaster {

    private volatile Sse sse;
    private volatile SseBroadcaster broadcaster;

    public synchronized void register(SseEventSink sink, Sse sse) { // 【1】
        if (broadcaster == null) {
            this.sse = sse;
            this.broadcaster = sse.newBroadcaster();
        }
        broadcaster.register(sink);
    }

    public void onTransferred(@ObservesAsync EmployeeTransferred event) { // 【2】
        if (broadcaster == null) {
            return;
        }
        broadcaster.broadcast(sse.newEventBuilder()
                .name("transferred") // 【3】
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .data(EmployeeTransferred.class, event)
                .build()); // 【4】
    }
}
