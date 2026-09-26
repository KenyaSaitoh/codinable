package pro.kensait.mybatis.company.mapper;

import pro.kensait.mybatis.company.dto.Department;
import pro.kensait.mybatis.company.dto.Employee;

/*
 * empdeptnestの契約を定義するインターフェース
 */
public interface EmpDeptNestMapper {

    // 社員の検索
    Employee selectEmployee(int employeeId);
    // 部署の検索
    Department selectDepartment(int departmentId);
}
