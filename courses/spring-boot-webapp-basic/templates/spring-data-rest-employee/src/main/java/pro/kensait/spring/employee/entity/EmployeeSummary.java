package pro.kensait.spring.employee.entity;

import org.springframework.data.rest.core.config.Projection;
/** ?projection=summaryで表示項目を絞る権限制御ではない */
@Projection(name = "summary", types = Employee.class)
public interface EmployeeSummary {
    // アクセサメソッド
    Integer getEmployeeId();

    // 社員コードの取得
    String getEmployeeCode();

    // 社員名へのアクセサメソッド
    String getEmployeeName();

    // アクセサメソッド
    Integer getDepartmentId();

    // アクセサメソッド
    Integer getJobId();
}
