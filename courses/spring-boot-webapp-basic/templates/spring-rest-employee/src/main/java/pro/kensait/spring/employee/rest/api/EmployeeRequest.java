package pro.kensait.spring.employee.rest.api;

import java.time.LocalDate;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import pro.kensait.spring.employee.rest.entity.Employee;
/** 更新可能な項目だけを受け取るID・社員コード・状態はサーバーが管理する */
public record EmployeeRequest(
        @NotBlank @Size(max = 30) String employeeName,
        @NotNull @Positive Integer departmentId,
        @NotNull @Positive Integer jobId,
        @NotNull @Min(0) @Max(9_999_999) Integer salary,
        @NotNull LocalDate entranceDate,
        @PositiveOrZero Integer version) {

    // 社員の初期化
    public EmployeeRequest {
        employeeName = employeeName == null ? null : employeeName.strip();
    }

    // リクエストを社員エンティティへの変換
    public Employee toEmployee() {
        Employee employee = new Employee();
        employee.setEmployeeName(employeeName);
        employee.setDepartmentId(departmentId);
        employee.setJobId(jobId);
        employee.setSalary(salary);
        employee.setEntranceDate(entranceDate);
        return employee;
    }
}
