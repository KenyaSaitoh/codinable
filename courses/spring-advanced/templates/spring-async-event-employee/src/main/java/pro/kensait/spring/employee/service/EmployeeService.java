package pro.kensait.spring.employee.service;

import java.time.LocalDateTime;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.kensait.spring.employee.entity.Employee;
import pro.kensait.spring.employee.event.EmployeeTransferred;
import pro.kensait.spring.employee.repository.EmployeeRepository;

/*
 * 社員の異動の登録
 * トランザクションの中ではイベントを発行するだけで、通知や履歴の記録はコミット後にリスナーが行う
 * 異動先の部署が無いときは、イベントを発行したあとのコミットで外部キー制約に違反してロールバックする
 */
@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final ApplicationEventPublisher publisher;

    public EmployeeService(EmployeeRepository employeeRepository, ApplicationEventPublisher publisher) {
        this.employeeRepository = employeeRepository;
        this.publisher = publisher;
    }

    @Transactional
    public Employee transfer(Integer employeeId, Integer toDepartmentId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        Integer fromDepartmentId = employee.getDepartmentId();
        employee.setDepartmentId(toDepartmentId); // 【1】
        publisher.publishEvent(new EmployeeTransferred( // 【2】
                employeeId, employee.getEmployeeName(),
                fromDepartmentId, toDepartmentId, LocalDateTime.now()));
        return employee;
    }
}
