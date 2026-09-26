package pro.kensait.mybatis.company.mapper;

import pro.kensait.mybatis.company.dto.Employee;

/*
 * 社員の契約を定義するインターフェース
 */
public interface EmployeeMapper {

    // 社員の検索
    Employee selectEmployee(int employeeId);
    // 社員pessimisticlockの検索
    Employee selectEmployeeWithPessimisticLock(int employeeId);

    // サービスメソッド：社員の更新
    int updateEmployee(Employee employee);
    // 社員optimisticlockの更新
    int updateEmployeeWithOptimisticLock(Employee employee);
}
