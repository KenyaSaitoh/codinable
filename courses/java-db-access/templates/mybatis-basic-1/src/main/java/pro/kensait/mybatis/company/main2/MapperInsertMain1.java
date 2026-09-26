package pro.kensait.mybatis.company.main2;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.Employee;
import pro.kensait.mybatis.company.mapper.EmployeeMapper;

/*
 * INSERT文
 */
public class MapperInsertMain1 {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // Mapperを取得する
            EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);

            // 保存対象のEmployeeを生成する
            Employee param = new Employee(10021, "Steve", "PLANNING", 380000);

            // INSERT文を発行しコミットする
            mapper.insertEmployee(param);
            sqlSession.commit();
        }
    }
}
