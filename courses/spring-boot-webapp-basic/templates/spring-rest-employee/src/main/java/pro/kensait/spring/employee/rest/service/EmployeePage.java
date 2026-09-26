package pro.kensait.spring.employee.rest.service;

import java.util.List;

import pro.kensait.spring.employee.rest.entity.Employee;
/** 永続化方式に依存しない社員一覧のページ */
public record EmployeePage(List<Employee> content, int totalPages, long totalElements) {
    // 社員の初期化
    public EmployeePage {
        content = List.copyOf(content);
    }

    // 呼び出し元による検索結果の変更の防止
    @Override
    public List<Employee> content() {
        return List.copyOf(content);
    }

    // 空の検索結果の生成
    public static EmployeePage empty() {
        return new EmployeePage(List.of(), 0, 0);
    }
}
