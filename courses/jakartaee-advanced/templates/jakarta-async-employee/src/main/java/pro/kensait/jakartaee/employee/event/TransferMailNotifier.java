package pro.kensait.jakartaee.employee.event;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/*
 * 異動をメールで知らせる非同期のオブザーバー（教材用。実際にはメールを送らず、ログに出すだけ）
 * microprofile-config.properties の transfer.mail.fail を true にすると例外を送出し、
 * 失敗が TransferEventRelay の exceptionally に集約されることを確かめられる（異動履歴の記録は止まらない）
 */
@ApplicationScoped
public class TransferMailNotifier {
    private static final Logger logger = Logger.getLogger(TransferMailNotifier.class.getName());

    @Inject
    @ConfigProperty(name = "transfer.mail.fail", defaultValue = "false")
    private boolean fail;

    public void notify(@ObservesAsync EmployeeTransferred event) {
        if (fail) {
            throw new IllegalStateException("メールサーバーに接続できません（教材用の失敗） employeeId="
                    + event.employeeId());
        }
        logger.info("[ TransferMailNotifier#notify ] 異動をメールで知らせました " + event.employeeName()
                + " " + event.fromDepartmentId() + " -> " + event.toDepartmentId()
                + " thread=" + Thread.currentThread());
    }
}
