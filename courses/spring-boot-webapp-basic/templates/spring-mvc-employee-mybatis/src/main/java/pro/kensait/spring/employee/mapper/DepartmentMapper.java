package pro.kensait.spring.employee.mapper;

import java.util.List;

import pro.kensait.spring.employee.entity.Department;

/*
 * 部署の契約を定義するインターフェース
 */
public interface DepartmentMapper {
    // 全件検索
    List<Department> findAll();

    // リポジトリメソッド：主キー検索によって社員の取得
    Department findById(Integer departmentId);
}
