package pro.kensait.jakartaee.employee.service;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import pro.kensait.jakartaee.employee.entity.Employee;

/*
 * 社員の取得。取得処理へ入った回数を、OpenTelemetry のカウンター employee.get.count で数える
 * （Telemetry を有効にしていないと、add を呼んでも何も記録されない。microprofile-config.properties 参照）
 */
@ApplicationScoped
public class EmployeeService {
    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private Meter meter; // 【1】

    private LongCounter getCounter;

    @PostConstruct
    void initMetrics() {
        getCounter = meter.counterBuilder("employee.get.count") // 【2】
                .setDescription("社員取得処理の呼び出し回数")
                .setUnit("{call}")
                .build();
    }

    public EmployeeTO getEmployee(Integer employeeId) {
        getCounter.add(1, Attributes.of(
                AttributeKey.stringKey("employee.type"), "regular")); // 【3】
        Employee employee = entityManager.find(Employee.class, employeeId);
        if (employee == null) {
            throw new EmployeeNotFoundException(
                    "社員が存在しません => " + employeeId);
        }
        return toEmployeeTO(employee);
    }

    public List<EmployeeTO> getEmployeesAll() {
        return entityManager.createQuery("SELECT e FROM Employee e ORDER BY e.employeeId", Employee.class)
                .getResultList().stream().map(this::toEmployeeTO).toList();
    }

    private EmployeeTO toEmployeeTO(Employee e) {
        return new EmployeeTO(e.getEmployeeId(), e.getEmployeeName(),
                e.getDepartment() == null ? null : e.getDepartment().getDepartmentId(),
                e.getDepartment() == null ? null : e.getDepartment().getDepartmentName(),
                e.getJobName(), e.getSalary(), e.getEntranceDate());
    }
}
