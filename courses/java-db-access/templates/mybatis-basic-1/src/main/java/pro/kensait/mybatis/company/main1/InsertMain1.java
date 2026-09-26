package pro.kensait.mybatis.company.main1;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.Employee;

/*
 * INSERT文
 */
public class InsertMain1 {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // 保存対象のEmployeeを生成する
            Employee param = new Employee(10021, "Steve", "PLANNING", 380000);

            // INSERT文を発行しコミットする
            sqlSession.insert("insertEmployee", param);
            sqlSession.commit();
        }
    }
}
