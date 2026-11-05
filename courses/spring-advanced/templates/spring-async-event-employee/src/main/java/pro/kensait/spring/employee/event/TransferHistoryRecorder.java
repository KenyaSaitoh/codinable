package pro.kensait.spring.employee.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pro.kensait.spring.employee.entity.TransferHistory;
import pro.kensait.spring.employee.repository.TransferHistoryRepository;

/*
 * 異動履歴を記録するリスナー
 * 発行側のトランザクションはコミット済みなので、履歴の書き込みには新しいトランザクションを使う
 */
@Component
public class TransferHistoryRecorder {
    private static final Logger logger = LoggerFactory.getLogger(TransferHistoryRecorder.class);

    private final TransferHistoryRepository historyRepository;

    public TransferHistoryRecorder(TransferHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Async("employeeExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 【1】
    public void record(EmployeeTransferred event) {
        historyRepository.save(new TransferHistory( // 【2】
                event.employeeId(), event.fromDepartmentId(),
                event.toDepartmentId(), event.occurredAt()));
        logger.info("[ TransferHistoryRecorder#record ] 異動履歴を記録しました employeeId={} thread={}",
                event.employeeId(), Thread.currentThread());
    }
}
