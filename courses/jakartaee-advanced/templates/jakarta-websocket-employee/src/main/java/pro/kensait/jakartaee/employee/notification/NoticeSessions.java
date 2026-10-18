package pro.kensait.jakartaee.employee.notification;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Session;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/*
 * 接続中の Session の一覧と、宛先に応じた配信
 * すべての接続から共有するので @ApplicationScoped にする
 */
@ApplicationScoped
public class NoticeSessions {
    private final Set<Session> sessions = ConcurrentHashMap.newKeySet(); // 【1】

    public void add(Session session) { sessions.add(session); }
    public void remove(Session session) { sessions.remove(session); }

    public void broadcast(EmployeeNotice notice) {
        for (Session session : sessions) { // 【2】
            sendTo(session, notice);
        }
    }

    public void sendTo(Session session, Object message) {
        if (!session.isOpen()) { // 【3】
            remove(session);
            return;
        }
        try {
            synchronized (session) { // 【4】
                session.getBasicRemote().sendObject(message);
            }
        } catch (IOException | EncodeException e) {
            remove(session); // 【5】
        }
    }
}
