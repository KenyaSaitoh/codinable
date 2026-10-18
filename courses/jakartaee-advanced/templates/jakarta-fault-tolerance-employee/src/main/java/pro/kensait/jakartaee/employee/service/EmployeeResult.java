package pro.kensait.jakartaee.employee.service;

import java.util.List;
import pro.kensait.jakartaee.employee.client.EmployeeTO;

/*
 * 社員一覧の取得結果。代替応答のときは fallback が true（空リストだけで「社員が 0 人」と判断しない）
 */
public record EmployeeResult(List<EmployeeTO> employees, boolean fallback, String message) {
}
