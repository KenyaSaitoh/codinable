package pro.kensait.jakartaee.employee.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;
import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import pro.kensait.jakartaee.employee.client.EmployeeApiClient;
import pro.kensait.jakartaee.employee.client.EmployeeTO;

/*
 * 社員APIの呼び出しに、Fault Tolerance のアノテーションで遮断・同時実行数・時間・再試行・代替応答を宣言する
 * アノテーションは CDI のインターセプタとして働くので、注入した参照を通して呼んだときだけ効く
 */
@ApplicationScoped
public class EmployeeClientService {
    private static final Logger logger = Logger.getLogger(EmployeeClientService.class.getName());

    @Inject
    @RestClient
    private EmployeeApiClient employeeApiClient;

    @CircuitBreaker(requestVolumeThreshold = 4, // 【1】
            failureRatio = 0.5, // 【2】
            delay = 10, delayUnit = ChronoUnit.SECONDS, // 【3】
            successThreshold = 2) // 【4】
    @Timeout(3000)
    @Fallback(fallbackMethod = "fallbackEmployees")
    public EmployeeResult getEmployees() {
        logger.info("[ EmployeeClientService#getEmployees ]");
        List<EmployeeTO> employees = employeeApiClient.findAll(); // 【5】
        return new EmployeeResult(employees, false, "取得成功");
    }

    public EmployeeResult fallbackEmployees() {
        logger.warning("[ EmployeeClientService#fallbackEmployees ]");
        return new EmployeeResult(List.of(), true,
                "社員APIを利用できないため代替応答を返しました");
    }

    @Bulkhead(2) // 【1】
    public EmployeeTO getEmployee(Integer employeeId) {
        logger.info("[ EmployeeClientService#getEmployee ]");
        return employeeApiClient.find(employeeId); // 【2】
    }

    @Asynchronous // 【3】
    @Bulkhead(value = 2, waitingTaskQueue = 5) // 【4】
    public CompletionStage<EmployeeTO> getEmployeeAsync(Integer employeeId) {
        logger.info("[ EmployeeClientService#getEmployeeAsync ]");
        return CompletableFuture.completedFuture(
                employeeApiClient.find(employeeId)); // 【5】
    }

    @Retry(maxRetries = 2, // 【1】
            delay = 1000, jitter = 200, // 【2】
            maxDuration = 10000, // 【3】
            retryOn = ProcessingException.class, // 【4】
            abortOn = WebApplicationException.class)
    @Timeout(3000)
    @Fallback(value = EmployeeFallbackHandler.class,
            skipOn = WebApplicationException.class)
    public EmployeeResult getEmployeesWithRetry() {
        logger.info("[ EmployeeClientService#getEmployeesWithRetry ]");
        List<EmployeeTO> employees = employeeApiClient.findAll(); // 【5】
        return new EmployeeResult(employees, false, "取得成功");
    }
}
