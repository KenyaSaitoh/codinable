package pro.kensait.jakartaee.employee.service;

import java.io.Serializable;
import java.util.List;

import pro.kensait.jakartaee.employee.entity.Employee;

/** 社員一覧の 1 ページ（そのページの社員・総ページ数・総件数） */
public record EmployeePage(List<Employee> content, int totalPages, long totalElements)
        implements Serializable {

    public EmployeePage {
        content = List.copyOf(content);
    }

    public static EmployeePage empty() {
        return new EmployeePage(List.of(), 0, 0);
    }
}
