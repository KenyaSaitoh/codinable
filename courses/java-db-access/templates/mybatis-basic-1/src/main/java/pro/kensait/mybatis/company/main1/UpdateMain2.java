package pro.kensait.mybatis.company.main1;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.EmployeeSalaryParam;

/*
 * UPDATE文、一括更新（パラメータ専用DTO）
 */
public class UpdateMain2 {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // UPDATE文を発行しコミットする
            EmployeeSalaryParam param = new EmployeeSalaryParam(350000, 1000);
            sqlSession.update("subtractSalaryWithParam", param);
            sqlSession.commit();
        }
    }
}
