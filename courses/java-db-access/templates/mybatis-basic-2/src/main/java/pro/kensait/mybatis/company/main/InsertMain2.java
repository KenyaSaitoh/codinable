package pro.kensait.mybatis.company.main;

import java.time.LocalDate;
import java.util.Calendar;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.Employee;
import pro.kensait.mybatis.company.mapper.EmployeeMapper;
import pro.kensait.mybatis.company.type.JobType;

/*
 * insertmain2の機能を提供するクラス
 */
public class InsertMain2 {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // Mapperを取得する
            EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);

            // 保存対象のEmployeeを生成する
            Calendar cal = Calendar.getInstance();
            cal.set(2017, 11, 1);
            Employee param = new Employee("Trent", "PRODUCT", LocalDate.now(),
                    JobType.CHIEF, 310000, 0L);

            // INSERT文を発行しコミットする
            mapper.insertEmployeeWithKeyGen(param);
            sqlSession.commit();

            // 自動採番されたキーを表示する
            System.out.println("employeeId => " + param.getEmployeeId());
        }
    }
}
