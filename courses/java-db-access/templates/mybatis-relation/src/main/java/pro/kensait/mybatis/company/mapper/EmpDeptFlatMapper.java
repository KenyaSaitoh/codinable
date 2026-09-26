package pro.kensait.mybatis.company.mapper;

import java.util.List;

import pro.kensait.mybatis.company.dto.EmpDept;

/*
 * empdeptflatの契約を定義するインターフェース
 */
public interface EmpDeptFlatMapper {

    // empdeptの検索
    EmpDept selectEmpDept(int employeeId);
    // empdept部署IDの検索
    List<EmpDept> selectEmpDeptByDepartmentId(int departmentId);
}
