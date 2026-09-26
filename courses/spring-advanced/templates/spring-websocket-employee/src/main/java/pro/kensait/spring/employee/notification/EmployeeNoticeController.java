package pro.kensait.spring.employee.notification;
import java.time.LocalDateTime;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
/*
 * 社員notice機能のコントローラー
 */
@Controller
public class EmployeeNoticeController {
    // notifyの実行
    @MessageMapping("/employees/notifications")
    @SendTo("/topic/employees")
    public EmployeeNotice notify(@Valid EmployeeNotice notice) {
        return new EmployeeNotice(notice.employeeId(), notice.employeeName(),
                notice.message(), LocalDateTime.now().toString());
    }
    // 業務上不正な入力の場合は400の返却
    @MessageExceptionHandler
    @SendToUser(value = "/queue/errors", broadcast = false)
    public String invalid(Exception error) {
        return "社員ID・社員名・通知内容を確認してください";
    }
}
