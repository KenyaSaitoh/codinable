package pro.kensait.db.mybatisdynamic;

import java.math.BigDecimal;

/*
 * 社員viewを表すレコード
 */
public record EmployeeView(int id, String name, String departmentName, BigDecimal salary) {
}
