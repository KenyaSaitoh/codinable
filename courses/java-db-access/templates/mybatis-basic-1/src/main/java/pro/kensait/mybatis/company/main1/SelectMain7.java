package pro.kensait.mybatis.company.main1;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.Employee;
/*
 * SELECT文、条件検索（Mapパラメータ使用）、複数件ヒット
 */
public class SelectMain7 {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // パラメータとなるMapを生成する
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("lowerSalary", 300000);
            param.put("upperSalary", 400000);

            // SELECT文を発行し結果を表示する
            List<Employee> result = sqlSession.selectList("selectEmployeesWithMap",
                    param);
            showEntityList(result);
        }
    }
}
