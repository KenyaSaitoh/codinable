package pro.kensait.mybatis.company.main1;

import static pro.kensait.jdbc.util.ResultUtil.*;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.Employee;
/*
 * SELECT文、主キー検索、ネーミングルールによる自動結果マッピング
 */
public class SelectMain3 {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // SELECT文を発行し結果を表示する
            Employee result = sqlSession.selectOne("selectEmployee3", 10005);
            showEntity(result);
        }
    }
}
