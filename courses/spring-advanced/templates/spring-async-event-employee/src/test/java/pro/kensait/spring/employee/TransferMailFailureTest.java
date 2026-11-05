package pro.kensait.spring.employee;

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import pro.kensait.spring.employee.repository.TransferHistoryRepository;
import pro.kensait.spring.employee.service.EmployeeService;

/*
 * メール通知が失敗しても、呼び出し元には届かず、異動履歴の記録は止まらないことを確かめる
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:hsqldb:mem:transferMailFailureTest",
        "transfer.mail.fail=true"})
@ExtendWith(OutputCaptureExtension.class)
class TransferMailFailureTest {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private TransferHistoryRepository historyRepository;

    @Test
    void mailFailureDoesNotStopHistory(CapturedOutput output) {
        employeeService.transfer(10006, 2);

        await().atMost(Duration.ofSeconds(5)).until(() -> historyRepository.findByEmployeeId(10006).size() == 1
                && output.getOut().contains("異動通知の一部が失敗しました"));
    }
}
