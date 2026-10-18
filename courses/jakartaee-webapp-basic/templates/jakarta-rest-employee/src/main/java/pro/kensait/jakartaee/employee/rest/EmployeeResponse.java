package pro.kensait.jakartaee.employee.rest;

import java.time.LocalDate;

import pro.kensait.jakartaee.employee.entity.Employee;

/** 社員 1 件の応答の本文。部署と役職は ID と名前で返す */
public record EmployeeResponse(
        Integer employeeId,
        String employeeCode,
        String employeeName,
        Integer departmentId,
        String departmentName,
        Integer jobId,
        String jobName,
        Integer salary,
        LocalDate entranceDate,
        String status,
        Integer version) {

    public static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getEmployeeId(),
                employee.getEmployeeCode(),
                employee.getEmployeeName(),
                employee.getDepartment() == null ? null : employee.getDepartment().getDepartmentId(),
                employee.getDepartment() == null ? null : employee.getDepartment().getDepartmentName(),
                employee.getJob().getJobId(),
                employee.getJob().getJobName(),
                employee.getSalary(),
                employee.getEntranceDate(),
                employee.getStatus(),
                employee.getVersion());
    }
}
