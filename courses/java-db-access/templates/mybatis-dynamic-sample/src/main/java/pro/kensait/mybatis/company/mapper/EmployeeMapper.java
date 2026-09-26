package pro.kensait.mybatis.company.mapper;

import java.util.List;

import pro.kensait.mybatis.company.dto.Employee;
import pro.kensait.mybatis.company.dto.EmployeeDynamicParam;

/*
 * 社員の契約を定義するインターフェース
 */
public interface EmployeeMapper {

    // dynamic社員の検索
    List<Employee> selectDynamicEmployees(EmployeeDynamicParam paramEmployee);
}
