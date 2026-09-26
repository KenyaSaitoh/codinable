package pro.kensait.mybatis.company.main1;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.Employee;

/*
 * DELETE文、一括削除（DTOパラメータ使用）
 */
public class DeleteMain2 {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // DELETE文を発行しコミットする
            Employee param = new Employee(null, null, "PRODUCT", 400000);
            sqlSession.delete("deleteEmployees", param);
            sqlSession.commit();
        }
    }
}
