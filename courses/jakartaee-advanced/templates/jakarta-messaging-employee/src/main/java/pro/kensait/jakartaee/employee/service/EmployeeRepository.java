package pro.kensait.jakartaee.employee.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pro.kensait.jakartaee.employee.entity.Employee;

/*
 * 社員の保存（Jakarta Persistence）。トランザクションは呼び出し側が決める
 */
@ApplicationScoped
public class EmployeeRepository {
    @PersistenceContext(unitName = "producerPU")
    private EntityManager em;

    // 保存して、採番された社員IDを確定させる（イベントに社員IDを入れるため flush する）
    public void persist(Employee employee) {
        em.persist(employee);
        em.flush();
    }
}
