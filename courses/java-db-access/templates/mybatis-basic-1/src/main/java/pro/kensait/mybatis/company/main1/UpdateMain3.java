package pro.kensait.mybatis.company.main1;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;

/*
 * UPDATE文、一括更新（Mapパラメータ使用）
 */
public class UpdateMain3 {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // UPDATE文を発行しコミットする
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("salary", 400000);
            param.put("payCut", 3000);
            sqlSession.update("subtractSalaryWithMap", param);
            sqlSession.commit();
        }
    }
}
