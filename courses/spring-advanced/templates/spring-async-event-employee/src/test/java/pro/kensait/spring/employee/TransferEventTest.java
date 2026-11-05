package pro.kensait.spring.employee;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;
import pro.kensait.spring.employee.event.TransferMailNotifier;
import pro.kensait.spring.employee.repository.EmployeeRepository;
import pro.kensait.spring.employee.repository.TransferHistoryRepository;
import pro.kensait.spring.employee.service.EmployeeService;

/*
 * イベントのリスナーは、コミットしたときだけ、コミットのあとで、別のスレッドで動くことを確かめる
 * データベースをテスト間で共有するので、テストごとに別の社員を異動する
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:hsqldb:mem:transferEventTest")
class TransferEventTest {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private TransferHistoryRepository historyRepository;
    @Autowired
    private TransferMailNotifier notifier;
    @Autowired
    private TransactionTemplate transaction;

    // コミットすると、通知と履歴の記録が別の仮想スレッドで動く
    @Test
    void commitNotifiesOnAnotherVirtualThread() {
        Thread caller = Thread.currentThread();
        employeeService.transfer(10002, 3);

        await().atMost(Duration.ofSeconds(5)).until(() -> sentFor(10002).size() == 1
                && historyRepository.findByEmployeeId(10002).size() == 1);
        TransferMailNotifier.Sent sent = sentFor(10002).get(0);
        assertEquals(1, sent.event().fromDepartmentId());
        assertEquals(3, sent.event().toDepartmentId());
        assertNotEquals(caller, sent.thread());
        assertTrue(sent.thread().isVirtual(), sent.thread().toString());
        assertEquals(3, employeeRepository.findById(10002).orElseThrow().getDepartmentId());
    }

    // イベントを発行しても、コミットするまでは通知されない
    @Test
    void notNotifiedBeforeCommit() {
        transaction.executeWithoutResult(status -> {
            employeeService.transfer(10005, 1); // 外側のトランザクションに参加するので、ここではコミットしない
            await().during(Duration.ofMillis(500)).atMost(Duration.ofSeconds(2))
                    .until(() -> sentFor(10005).isEmpty());
        });
        await().atMost(Duration.ofSeconds(5)).until(() -> sentFor(10005).size() == 1);
    }

    // 異動先の部署が無いと、イベントを発行したあとのコミットで失敗する。通知も履歴の記録も起きない
    @Test
    void constraintViolationAtCommitDoesNotNotify() {
        assertThrows(DataIntegrityViolationException.class, () -> employeeService.transfer(10003, 99));

        assertNeverNotified(10003);
        assertEquals(2, employeeRepository.findById(10003).orElseThrow().getDepartmentId());
    }

    // 呼び出し側がロールバックしても、通知も履歴の記録も起きない
    @Test
    void outerRollbackDoesNotNotify() {
        transaction.executeWithoutResult(status -> {
            employeeService.transfer(10004, 1);
            status.setRollbackOnly();
        });

        assertNeverNotified(10004);
        assertEquals(3, employeeRepository.findById(10004).orElseThrow().getDepartmentId());
    }

    private void assertNeverNotified(int employeeId) {
        await().during(Duration.ofSeconds(1)).atMost(Duration.ofSeconds(3))
                .until(() -> sentFor(employeeId).isEmpty()
                        && historyRepository.findByEmployeeId(employeeId).isEmpty());
    }

    private List<TransferMailNotifier.Sent> sentFor(int employeeId) {
        return notifier.sent().stream().filter(s -> s.event().employeeId() == employeeId).toList();
    }
}
