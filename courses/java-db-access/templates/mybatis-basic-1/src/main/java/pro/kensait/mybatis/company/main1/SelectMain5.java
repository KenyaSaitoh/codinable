package pro.kensait.mybatis.company.main1;

import static pro.kensait.jdbc.util.ResultUtil.*;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;
/*
 * SELECT COUNT文
 */
public class SelectMain5 {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // SELECT文を発行し結果を表示する
            Long result = sqlSession.selectOne("selectEmpCountByDept", "PLANNING");
            showSingleResult(result);
        }
    }
}
