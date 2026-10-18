package pro.kensait.jakartaee.employee.api;

import java.time.LocalDate;
import pro.kensait.jakartaee.employee.entity.Employee;
import pro.kensait.jakartaee.employee.service.DepartmentSummary;

/*
 * 画面用の社員（API の応答と SSE の data に使う）
 */
public record EmployeeView(Integer employeeId, String employeeName, Integer departmentId,
        String departmentName, String jobName, Integer salary, LocalDate entranceDate) {

    public static EmployeeView from(Employee e) {
        return new EmployeeView(e.getEmployeeId(), e.getEmployeeName(), e.getDepartmentId(),
                DepartmentSummary.departmentName(e.getDepartmentId()), e.getJobName(), e.getSalary(),
                e.getEntranceDate());
    }
}
