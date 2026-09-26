package pro.kensait.spring.employee.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import pro.kensait.spring.employee.entity.Employee;
import pro.kensait.spring.employee.entity.EmployeeSearchCriteria;

/*
 * 社員の契約を定義するインターフェース
 */
public interface EmployeeMapper {
    // データの検索
    List<Employee> search(@Param("criteria") EmployeeSearchCriteria criteria,
            @Param("offset") int offset,
            @Param("limit") int limit);

    // 件数の実行
    long count(@Param("criteria") EmployeeSearchCriteria criteria);

    // リポジトリメソッド：主キー検索によって社員の取得
    Employee findById(Integer employeeId);

    // 社員の登録
    void insert(Employee employee);

    // コードの更新
    void updateCode(Employee employee);

    // 一件更新
    void update(Employee employee);

    // 状態の更新
    void updateStatus(Employee employee);
}
