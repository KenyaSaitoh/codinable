package pro.kensait.jakartaee.employee.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import pro.kensait.jakartaee.employee.entity.Employee;

/*
 * 社員を登録し、同じトランザクションで社員イベントを送る
 * 給与の範囲の検証に失敗すると、登録も送信もロールバックされる（登録されていない社員のイベントは届かない）
 */
@ApplicationScoped
public class EmployeeRegistrationService {
    private static final int MIN_SALARY = 150000;
    private static final int MAX_SALARY = 1000000;

    @Inject
    private EmployeeRepository employeeRepository;
    @Inject
    private EventSendService eventSendService;

    @Transactional // 【1】
    public Employee register(Employee employee) {
        employeeRepository.persist(employee); // 【2】
        eventSendService.send(EmployeeEvent.created(employee)); // 【3】
        validateSalaryBand(employee); // 【4】
        return employee;
    }

    // 業務上の検証。範囲外なら実行時例外を送出し、トランザクション全体をロールバックさせる
    private void validateSalaryBand(Employee employee) {
        Integer salary = employee.getSalary();
        if (salary == null || salary < MIN_SALARY || salary > MAX_SALARY) {
            throw new SalaryBandException("給与は " + MIN_SALARY + "〜" + MAX_SALARY + " の範囲で指定してください => " + salary);
        }
    }
}
