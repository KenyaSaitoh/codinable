package pro.kensait.mybatis.company.main;

import java.time.LocalDate;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.Department;
import pro.kensait.mybatis.company.dto.Employee;
import pro.kensait.mybatis.company.mapper.EmployeeMapper;
import pro.kensait.mybatis.company.type.JobType;

/*
 * INSERTのカスケード
 */
public class InsertMain {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // Mapperを取得する
            EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);

            // 保存対象のDepartmentインスタンスを生成する
            Department department = new Department();
            department.setDepartmentId(3);

            // 保存対象のEmployeeインスタンスを生成する
            Employee employee = new Employee(10021, "Steve", department, LocalDate.now(),
                    JobType.CHIEF, 500000);

            // INSERT文を発行しコミットする
            mapper.insertEmployee(employee);
            sqlSession.commit();
        }
    }
}
