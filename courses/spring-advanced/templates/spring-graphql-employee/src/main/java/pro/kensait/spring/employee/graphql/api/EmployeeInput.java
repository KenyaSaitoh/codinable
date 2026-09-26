package pro.kensait.spring.employee.graphql.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PositiveOrZero;

/*
 * 社員の作成・更新のための入力オブジェクト
 * （スキーマの「input EmployeeInput」に対応し、引数バインディングで変換される）
 */
public record EmployeeInput(
        @NotBlank
        @Size(min = 1, max = 20)
        String employeeName,    // 社員名
        @NotNull
        Integer departmentId,   // 部署ID
        String jobName,         // 職種
        @NotNull @PositiveOrZero Integer salary,         // 給与
        String entranceDate         // 入社日（"yyyy-MM-dd"形式の文字列）
        ) {
}
