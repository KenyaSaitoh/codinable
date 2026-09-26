package pro.kensait.mybatis.company.mapper;

import java.util.List;

import pro.kensait.mybatis.company.dto.Employee;

/*
 * 社員の契約を定義するインターフェース
 */
public interface EmployeeMapper {

    // 社員の検索
    Employee selectEmployee(int employeeId);
    // various部署の検索
    List<Employee> selectVariousDepartment(List<String> departmentNameList);

    // 社員の登録
    void insertEmployee(Employee employee);
    // 社員キーgenの登録
    void insertEmployeeWithKeyGen(Employee employee);
}
