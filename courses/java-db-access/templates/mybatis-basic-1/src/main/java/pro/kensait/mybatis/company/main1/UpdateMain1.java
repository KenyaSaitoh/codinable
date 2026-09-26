package pro.kensait.mybatis.company.main1;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.Employee;

/*
 * UPDATE文、主キー特定カラム更新
 */
public class UpdateMain1 {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // SELECT文を発行し結果を表示する
            Employee employee = sqlSession.selectOne("selectEmployee", 10005);

            // UPDATE文を発行しコミットする
            employee.setSalary(employee.getSalary() + 3333);
            sqlSession.update("updateEmployee", employee);
            sqlSession.commit();
        }
    }
}
