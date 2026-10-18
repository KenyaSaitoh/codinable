package pro.kensait.jakartaee.employee.service;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Logger;
import pro.kensait.jakartaee.employee.entity.Employee;

/*
 * 社員の取得を、@WithSpan と Tracer で計装する
 * HTTP サーバーのスパン（自動計装）の子として、employee.get・employee.get-all のスパンが記録される
 */
@ApplicationScoped
public class EmployeeService {
    private static final Logger logger =
            Logger.getLogger(EmployeeService.class.getName());

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private Tracer tracer; // 【1】

    @WithSpan("employee.get") // 【1】
    public EmployeeTO getEmployee(
            @SpanAttribute("employee.id") Integer employeeId) { // 【2】
        logWithTrace("[ EmployeeService#getEmployee ]");
        Span.current().setAttribute("employee.type", "regular"); // 【3】
        Employee employee = entityManager.find(Employee.class, employeeId);
        if (employee == null) {
            throw new EmployeeNotFoundException(
                    "社員が存在しません => " + employeeId);
        }
        return toEmployeeTO(employee); // 【4】
    }

    public List<EmployeeTO> getEmployeesAll() {
        Span span = tracer.spanBuilder("employee.get-all").startSpan(); // 【2】
        try (Scope scope = span.makeCurrent()) { // 【3】
            List<EmployeeTO> employees = entityManager
                    .createQuery("SELECT e FROM Employee e ORDER BY e.employeeId",
                            Employee.class)
                    .getResultList().stream().map(this::toEmployeeTO).toList();
            span.setAttribute("employee.count", employees.size());
            return employees;
        } catch (RuntimeException ex) {
            span.recordException(ex); // 【4】
            span.setStatus(StatusCode.ERROR);
            throw ex;
        } finally {
            span.end(); // 【5】
        }
    }

    // ログにトレースIDとスパンIDを含め、トレースと同じ要求のログを探せるようにする
    private void logWithTrace(String message) {
        SpanContext spanContext = Span.current().getSpanContext(); // 【1】
        logger.info(() -> message
                + " traceId=" + spanContext.getTraceId() // 【2】
                + " spanId=" + spanContext.getSpanId()); // 【3】
    }

    private EmployeeTO toEmployeeTO(Employee e) {
        return new EmployeeTO(e.getEmployeeId(), e.getEmployeeName(),
                e.getDepartment() == null ? null : e.getDepartment().getDepartmentId(),
                e.getDepartment() == null ? null : e.getDepartment().getDepartmentName(),
                e.getJobName(), e.getSalary(), e.getEntranceDate());
    }
}
