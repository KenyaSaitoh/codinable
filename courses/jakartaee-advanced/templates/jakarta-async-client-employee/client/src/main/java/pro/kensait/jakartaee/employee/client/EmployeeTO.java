package pro.kensait.jakartaee.employee.client;

import java.time.LocalDate;

/*
 * 社員を表すレコード（社員APIが返す JSON に対応）
 * withId・withSalary は、値を差し替えたコピーを作る
 */
public record EmployeeTO(
        Integer employeeId,      // 社員ID
        String employeeName,     // 社員名
        Integer departmentId,    // 部署ID
        String departmentName,   // 部署名
        String jobName,          // 職種
        Integer salary,          // 給与
        LocalDate entranceDate) { // 入社日

    public EmployeeTO withId(Integer id) {
        return new EmployeeTO(id, employeeName, departmentId, departmentName, jobName, salary, entranceDate);
    }

    public EmployeeTO withSalary(Integer newSalary) {
        return new EmployeeTO(employeeId, employeeName, departmentId, departmentName, jobName, newSalary, entranceDate);
    }
}
