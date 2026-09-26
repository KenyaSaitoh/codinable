package pro.kensait.db.jdbc;

import java.math.BigDecimal;

/*
 * 社員を表すレコード（部署情報をフラットに保持する）
 */
public record Employee(int id, int departmentId, String name, BigDecimal salary) {
}
