package pro.kensait.jakartaee.employee.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Startup;

/*
 * 起動状態のチェック: アプリケーションのスコープの準備が整ったか（/health/started と /health に出る）
 * @Startup は org.eclipse.microprofile.health.Startup（jakarta.ejb.Startup や CDI の Startup イベントとは別）
 */
@Startup // 【1】
@ApplicationScoped
public class StartupHealthCheck implements HealthCheck {

    private volatile boolean initialized; // 【2】

    void onInitialized(@Observes @Initialized(ApplicationScoped.class)
            Object event) { // 【3】
        initialized = true;
    }

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse.named("initialization")
                .status(initialized) // 【4】
                .build();
    }
}
