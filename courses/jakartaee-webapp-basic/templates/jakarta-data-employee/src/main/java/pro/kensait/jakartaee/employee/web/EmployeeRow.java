package pro.kensait.jakartaee.employee.web;

import java.io.Serializable;
import java.time.LocalDate;

/** 一覧画面の 1 行（部署名・役職名を解決した表示用の値） */
public record EmployeeRow(Integer employeeId, String employeeCode, String employeeName,
        String departmentName, String jobName, Integer salary, LocalDate entranceDate)
        implements Serializable {
}
