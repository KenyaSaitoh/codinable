package pro.kensait.jakartaee.employee.service;

import jakarta.enterprise.context.Dependent;
import java.util.List;
import java.util.logging.Logger;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;

/*
 * 再試行を使い切ったときなどの代替処理。失敗したメソッドと例外をログに残す
 */
@Dependent
public class EmployeeFallbackHandler
        implements FallbackHandler<EmployeeResult> { // 【1】

    private static final Logger logger =
            Logger.getLogger(EmployeeFallbackHandler.class.getName());

    @Override
    public EmployeeResult handle(ExecutionContext context) { // 【2】
        Throwable failure = context.getFailure(); // 【3】
        logger.warning(() -> "[ EmployeeFallbackHandler#handle ] "
                + context.getMethod().getName() + " => "
                + failure.getClass().getSimpleName());
        return new EmployeeResult(List.of(), true, // 【4】
                "社員APIを利用できないため代替応答を返しました");
    }
}
