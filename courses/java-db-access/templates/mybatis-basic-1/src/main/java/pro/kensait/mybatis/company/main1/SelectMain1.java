package pro.kensait.mybatis.company.main1;

import static pro.kensait.jdbc.util.ResultUtil.*;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.Employee;
/*
 * SELECT文、主キー検索、ResultMapによる結果マッピング
 */
public class SelectMain1 {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // SELECT文を発行し結果を表示する
            Employee result1 = sqlSession.selectOne("selectEmployee", 10005);
            showEntity(result1);

            // SELECT文を発行し結果を表示する
            // Employeeを単独で受け取るメソッドの場合、ヒットしない場合はnullが返される
            Employee result2 = sqlSession.selectOne("selectEmployee", 99999);
            showSingleResult(result2);
        }
    }
}
