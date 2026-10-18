package pro.kensait.jakartaee.employee.rest;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import pro.kensait.jakartaee.employee.entity.Department;
import pro.kensait.jakartaee.employee.entity.Employee;
import pro.kensait.jakartaee.employee.entity.Job;

/** 登録・更新の本文を受け取る入力専用の DTO。社員ID・社員コード・状態は受け取らない */
public record EmployeeRequest(
        @NotBlank @Size(max = 30) String employeeName,
        @NotNull @Positive Integer departmentId,
        @NotNull @Positive Integer jobId,
        @NotNull @Min(0) @Max(9_999_999) Integer salary,
        @NotNull LocalDate entranceDate,
        @PositiveOrZero Integer version) {

    public EmployeeRequest {
        employeeName = employeeName == null ? null : employeeName.strip();
    }

    public Employee toEmployee(Department department, Job job) {
        Employee employee = new Employee();
        employee.setEmployeeName(employeeName);
        employee.setDepartment(department);
        employee.setJob(job);
        employee.setSalary(salary);
        employee.setEntranceDate(entranceDate);
        return employee;
    }
}
