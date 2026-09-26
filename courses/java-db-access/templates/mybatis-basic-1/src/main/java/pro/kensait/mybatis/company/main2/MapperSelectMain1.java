package pro.kensait.mybatis.company.main2;

import static pro.kensait.jdbc.util.ResultUtil.*;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.Employee;
import pro.kensait.mybatis.company.mapper.EmployeeMapper;
/*
 * SELECT文、主キー検索、ResultMapによる結果マッピング
 */
public class MapperSelectMain1 {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // Mapperを取得する
            EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);

            // SELECT文を発行し結果を表示する
            Employee result = mapper.selectEmployee(10005);
            showEntity(result);
        }
    }
}
