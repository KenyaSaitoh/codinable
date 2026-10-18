package pro.kensait.jakartaee.employee.event;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.logging.Logger;
import pro.kensait.jakartaee.employee.entity.TransferHistory;
import pro.kensait.jakartaee.employee.repository.TransferHistoryRepository;

/*
 * 異動履歴を記録する非同期のオブザーバー
 */
@ApplicationScoped
public class TransferHistoryRecorder {
    private static final Logger logger = Logger.getLogger(TransferHistoryRecorder.class.getName());

    @Inject
    private TransferHistoryRepository historyRepository;

    @Transactional(Transactional.TxType.REQUIRES_NEW) // 【1】
    public void record(@ObservesAsync EmployeeTransferred event) { // 【2】
        historyRepository.save(new TransferHistory( // 【3】
                event.employeeId(), event.fromDepartmentId(),
                event.toDepartmentId(), event.occurredAt()));
        logger.info("[ TransferHistoryRecorder#record ] 異動履歴を記録しました employeeId=" + event.employeeId()
                + " thread=" + Thread.currentThread());
    }
}
