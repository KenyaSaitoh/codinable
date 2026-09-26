package pro.kensait.mybatis.company.mapper;

import pro.kensait.mybatis.company.dto.Employee;

/*
 * 社員の契約を定義するインターフェース
 */
public interface EmployeeMapper {

    // 社員の登録
    void insertEmployee(Employee employee);

    // サービスメソッド：社員の更新
    int updateEmployee(Employee employee);
}
