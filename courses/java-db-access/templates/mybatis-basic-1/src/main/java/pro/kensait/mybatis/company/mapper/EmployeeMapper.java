package pro.kensait.mybatis.company.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import pro.kensait.mybatis.company.dto.Employee;

/*
 * 社員の契約を定義するインターフェース
 */
public interface EmployeeMapper {

    // 社員の検索
    Employee selectEmployee(int employeeId);
    // 社員の検索
    List<Employee> selectEmployees(
            @Param("departmentName") String departmentName,
            @Param("salary") int salary);

    // 社員の登録
    void insertEmployee(Employee employee);

    // ミューテーションメソッド：Employeeの削除
    int deleteEmployee(int employeeId);

    // サービスメソッド：社員の更新
    int updateEmployee(Employee employee);
    // subtract月給を用いたパラメータの実行
    int subtractSalaryWithParam(
            @Param("salary") int salary,
            @Param("payCut") int payCut);
}
