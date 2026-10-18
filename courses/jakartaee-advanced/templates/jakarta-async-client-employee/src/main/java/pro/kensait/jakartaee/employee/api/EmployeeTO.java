package pro.kensait.jakartaee.employee.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/*
 * 社員を表すレコード（部署の情報を平らに持つ）
 * 登録・更新の入力では、社員ID と部署名はサーバーが決める
 */
public record EmployeeTO(
        Integer employeeId,                               // 社員ID
        @NotBlank @Size(max = 20) String employeeName,    // 社員名
        @NotNull Integer departmentId,                    // 部署ID
        String departmentName,                            // 部署名
        @Size(max = 40) String jobName,                   // 職種
        @NotNull @PositiveOrZero Integer salary,          // 給与
        LocalDate entranceDate) {                         // 入社日
}
