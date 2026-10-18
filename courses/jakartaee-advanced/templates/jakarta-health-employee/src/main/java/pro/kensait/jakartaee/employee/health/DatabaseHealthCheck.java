package pro.kensait.jakartaee.employee.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

/*
 * 準備状態のチェック: データベースへ接続して社員テーブルを参照できるか（/health/ready と /health に出る）
 */
@Readiness // 【1】
@ApplicationScoped
public class DatabaseHealthCheck implements HealthCheck {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public HealthCheckResponse call() { // 【2】
        try {
            Long count = entityManager
                    .createQuery("SELECT COUNT(e) FROM Employee e", Long.class)
                    .getSingleResult(); // 【3】
            return HealthCheckResponse.named("database")
                    .up()
                    .withData("employeeCount", count) // 【4】
                    .build();
        } catch (RuntimeException ex) {
            return HealthCheckResponse.named("database")
                    .down()
                    .withData("error", ex.getClass().getSimpleName()) // 【5】
                    .build();
        }
    }
}
