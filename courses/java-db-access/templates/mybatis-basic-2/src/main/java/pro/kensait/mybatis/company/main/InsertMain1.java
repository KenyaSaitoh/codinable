package pro.kensait.mybatis.company.main;

import java.time.LocalDate;
import java.util.Calendar;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.Employee;
import pro.kensait.mybatis.company.mapper.EmployeeMapper;
import pro.kensait.mybatis.company.type.JobType;

/*
 * INSERT文
 */
public class InsertMain1 {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // Mapperを取得する
            EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);

            // 保存対象のEmployeeを生成する
            Calendar cal = Calendar.getInstance();
            cal.set(2017, 11, 1);
            Employee param = new Employee(10021, "Steve", null, LocalDate.now(),
                    JobType.LEADER, 380000, 0L);

            // INSERT文を発行しコミットする
            mapper.insertEmployee(param);
            sqlSession.commit();
        }
    }
}
