package pro.kensait.db.mybatisdynamic;

import java.math.BigDecimal;

/*
 * 部署summaryを表すレコード
 */
public record DepartmentSummary(int departmentId, String departmentName, int employeeCount,
        BigDecimal averageSalary) {
}
