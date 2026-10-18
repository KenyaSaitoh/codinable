package pro.kensait.jakartaee.employee.notification;

import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.websocket.CloseReason;
import jakarta.websocket.DecodeException;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.time.LocalDateTime;
import java.util.logging.Logger;

/*
 * 社員通知の WebSocket エンドポイント（ws://localhost:8080/jakarta-websocket-employee/ws/employees）
 * 接続 1 本ごとにインスタンスが作られる。全員への配信は、共有の NoticeSessions が受け持つ
 */
@ServerEndpoint(value = "/ws/employees", // 【1】
        decoders = EmployeeNoticeDecoder.class,
        encoders = {EmployeeNoticeEncoder.class, NoticeErrorEncoder.class},
        configurator = OriginCheckConfigurator.class)
public class EmployeeNoticeEndpoint {
    private static final Logger logger = Logger.getLogger(EmployeeNoticeEndpoint.class.getName());

    @Inject
    NoticeSessions sessions; // 【2】
    @Inject
    Validator validator;

    @OnOpen
    public void open(Session session) { // 【3】
        sessions.add(session);
        logger.info("[ EmployeeNoticeEndpoint#open ] session=" + session.getId());
    }

    @OnMessage
    public void notify(EmployeeNotice notice, Session session) { // 【4】
        if (!validator.validate(notice).isEmpty()) {
            sessions.sendTo(session, NoticeError.INVALID);
            return;
        }
        sessions.broadcast(notice.withOccurredAt(LocalDateTime.now()));
    }

    @OnClose
    public void close(Session session, CloseReason reason) { // 【5】
        sessions.remove(session);
        logger.info("[ EmployeeNoticeEndpoint#close ] session=" + session.getId() + " reason=" + reason);
    }

    @OnError
    public void error(Session session, Throwable error) { // 【6】
        logger.info("[ EmployeeNoticeEndpoint#error ] " + error);
        if (error instanceof DecodeException && session.isOpen()) {
            sessions.sendTo(session, NoticeError.INVALID);
        }
    }
}
