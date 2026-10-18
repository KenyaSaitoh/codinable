package pro.kensait.jakartaee.employee.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import pro.kensait.jakartaee.employee.entity.Employee;
import pro.kensait.jakartaee.employee.event.EmployeeTransferred;
import pro.kensait.jakartaee.employee.repository.EmployeeRepository;

/*
 * 社員の異動の登録
 * トランザクションの中では同期イベント（fire）だけを発火し、非同期の処理はコミット後に TransferEventRelay が始める
 */
@ApplicationScoped
public class EmployeeService {
    @Inject
    private EmployeeRepository employeeRepository;

    @Inject
    private Event<EmployeeTransferred> transferredEvent; // 【1】

    @Transactional
    public Employee transfer(Integer employeeId, Integer toDepartmentId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        Integer fromDepartmentId = employee.getDepartmentId();
        employee.setDepartmentId(toDepartmentId); // 【2】
        transferredEvent.fire(new EmployeeTransferred( // 【3】
                employeeId, employee.getEmployeeName(),
                fromDepartmentId, toDepartmentId, LocalDateTime.now()));
        return employee;
    }
}
