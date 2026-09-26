package pro.kensait.mybatis.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.EmpDept;
import pro.kensait.mybatis.company.mapper.EmpDeptFlatMapper;

/*
 * ジョインしてフラットオブジェクトを取得する
 */
public class FlatSelectMain2 {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // Mapperを取得する
            EmpDeptFlatMapper mapper = sqlSession.getMapper(EmpDeptFlatMapper.class);

            // SELECT文を発行し結果を表示する
            List<EmpDept> resultList = mapper.selectEmpDeptByDepartmentId(3);
            showEntity(resultList);
        }
    }
}
