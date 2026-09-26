package pro.kensait.spring.employee.resilience.web;
import java.util.Map;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
/*
 * circuit状態APIの機能を提供するクラス
 */
@RestController
public class CircuitStatusApi {
    private final CircuitBreakerRegistry registry;
    // circuit状態APIの初期化
    public CircuitStatusApi(CircuitBreakerRegistry registry) {
        this.registry = registry;
    }
    // 状態の実行
    @GetMapping("/resilience-status")
    public Map<String, Object> status() {
        var breaker = registry.circuitBreaker("employeeApi");
        return Map.of("state", breaker.getState().name(),
                "failedCalls", breaker.getMetrics().getNumberOfFailedCalls(),
                "failureRate", breaker.getMetrics().getFailureRate());
    }
}
