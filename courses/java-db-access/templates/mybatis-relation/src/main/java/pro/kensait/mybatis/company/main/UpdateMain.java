package pro.kensait.mybatis.company.main;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.Employee;
import pro.kensait.mybatis.company.mapper.EmpDeptJoinMapper;
import pro.kensait.mybatis.company.mapper.EmployeeMapper;
import pro.kensait.mybatis.company.type.JobType;

/*
 * UPDATE（社員の属性変更）
 */
public class UpdateMain {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // Mapperを取得する
            EmpDeptJoinMapper mapper1 = sqlSession.getMapper(
                    EmpDeptJoinMapper.class);
            EmployeeMapper mapper2 = sqlSession.getMapper(EmployeeMapper.class);

            // 社員の属性を変更する
            Employee employee = mapper1.selectEmployee(10001);
            employee.setSalary(510000);
            employee.setJobType(JobType.LEADER);
            employee.getDepartment().setDepartmentId(3); // これがポイント!

            // UPDATE文を発行しコミットする
            mapper2.updateEmployee(employee);
            sqlSession.commit();
        }
    }
}
