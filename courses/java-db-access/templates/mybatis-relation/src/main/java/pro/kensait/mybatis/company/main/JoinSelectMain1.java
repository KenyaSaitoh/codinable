package pro.kensait.mybatis.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.Employee;
import pro.kensait.mybatis.company.mapper.EmpDeptJoinMapper;

/*
 * ジョインセレクトによって構造化オブジェクトを取得する
 * （ここではMany-to-OneのMany側、すなわちEmployeeを取得）
 */
public class JoinSelectMain1 {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // Mapperを取得する
            EmpDeptJoinMapper mapper = sqlSession.getMapper(EmpDeptJoinMapper.class);

            // SELECT文を発行し、単一のMany側（Employee）を取得する
            Employee employee = mapper.selectEmployee(10001);

            // 結果を表示する
            showEntity(employee);
        }
    }
}
