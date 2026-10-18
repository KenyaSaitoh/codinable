package pro.kensait.jakartaee.employee.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/*
 * 社員を表すレコード（部署の情報を平らに持つ）
 * ログイン用のアカウント（APP_USER）とは別に管理する
 */
public record Employee(
        Integer employeeId,                               // 社員ID
        @NotBlank @Size(max = 20) String employeeName,    // 社員名
        @NotNull Integer departmentId,                    // 部署ID
        String departmentName,                            // 部署名
        @Size(max = 40) String jobName,                   // 職種
        @NotNull @PositiveOrZero Integer salary,          // 給与
        LocalDate entranceDate) {                         // 入社日
}
