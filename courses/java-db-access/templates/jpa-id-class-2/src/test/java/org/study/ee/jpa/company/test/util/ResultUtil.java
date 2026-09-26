package org.study.ee.jpa.company.test.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;

import pro.kensait.jpa.company.entity.Employee;

/*
 * 結果の機能を提供するクラス
 */
public class ResultUtil {

    // 社員の表示
    public static void showEmployee(Employee employee) {
        System.out.print(employee.getSubsidiary().getSubsidiaryName() + " / ");
        System.out.print(employee.getEmployeeId() + " / ");
        System.out.print(employee.getEmployeeName() + " / ");
        System.out.println(employee.getSalary());
    }

    // 社員一覧の表示
    public static void showEmployeeList(List<Employee> list) {
        Iterator<Employee> i = list.iterator();
        while (i.hasNext()) {
            Employee employee = i.next();
            showEmployee(employee);
        }
    }

    // 社員変換元データベースの表示
    public static void showEmployeeFromDatabase(Integer subsidiaryId, Integer employeeId) {
        String url = PropertyUtil.getValue("jdbc.url");
        String user = PropertyUtil.getValue("jdbc.user");
        String password = PropertyUtil.getValue("jdbc.password");
        try (Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT * FROM EMPLOYEE WHERE SUBSIDIARY_ID = ? " +
                    "AND EMPLOYEE_ID = ?")) {
            pstmt.setInt(1, subsidiaryId);
            pstmt.setInt(2, employeeId);
            try (ResultSet rset = pstmt.executeQuery()) {
                while (rset.next()) {
                    System.out.println(rset.getInt(1) + " / " + rset.getInt(2)
                            + " / " + rset.getString(3) + " / " + rset.getInt(4));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("DBの再取得に失敗しました", e);
        }
    }
}
