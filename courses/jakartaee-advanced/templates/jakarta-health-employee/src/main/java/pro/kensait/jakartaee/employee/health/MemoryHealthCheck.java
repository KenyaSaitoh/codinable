package pro.kensait.jakartaee.employee.health;

import jakarta.enterprise.context.ApplicationScoped;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

/*
 * 生存状態のチェック: ヒープメモリの使用率が 90% に達していないか（/health/live と /health に出る）
 * 外部の依存先（データベースなど）には左右されない、プロセス自身の状態だけを調べる
 */
@Liveness
@ApplicationScoped
public class MemoryHealthCheck implements HealthCheck {
    private static final double THRESHOLD = 0.9;

    @Override
    public HealthCheckResponse call() {
        MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long used = heap.getUsed();
        long max = heap.getMax();
        boolean healthy = max <= 0 || (double) used / max < THRESHOLD;
        return HealthCheckResponse.named("heap-memory")
                .status(healthy)
                .withData("usedBytes", used)
                .withData("maxBytes", max)
                .build();
    }
}
