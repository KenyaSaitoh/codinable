package pro.kensait.mybatis.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.Department;
import pro.kensait.mybatis.company.dto.Employee;
import pro.kensait.mybatis.company.mapper.EmpDeptJoinMapper;

/*
 * ジョインセレクトによって構造化オブジェクトを取得する
 * （ここではOne-to-manyのOne側、すなわちDepartmentを取得）
 */
public class JoinSelectMain2 {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // Mapperを取得する
            EmpDeptJoinMapper mapper = sqlSession.getMapper(EmpDeptJoinMapper.class);

            // SELECT文を発行し、One側（Department）を取得する
            Department department = mapper.selectDepartment(3);

            // Departmentオブジェクトに関連を持つEmployeeのリストを取得して表示する
            List<Employee> resultList = department.getEmployees();
            showEntity(resultList);
        }
    }
}
