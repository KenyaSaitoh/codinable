package pro.kensait.jakartaee.employee.graphql.api;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Input;
import org.eclipse.microprofile.graphql.NonNull;

/*
 * 社員の登録・更新の入力型（スキーマの input EmployeeInput）
 * 採番される ID や部署オブジェクトそのものは入力させず、部署 ID を受け取る
 */
@Input("EmployeeInput") // 【1】
public record EmployeeInput(
        @NonNull @Size(max = 20) String employeeName, // 【2】
        @NonNull @Id Integer departmentId,
        String jobName,
        @NonNull @PositiveOrZero Integer salary, // 【3】
        LocalDate entranceDate) { // 【4】
}
