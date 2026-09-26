package pro.kensait.mybatis.company.main1;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.Employee;
/*
 * SELECT文、条件検索（DTOパラメータ使用）、複数件ヒット
 */
public class SelectMain6 {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // パラメータとなるEmployeeを生成する
            Employee param1 = new Employee(null, null, "SALES", 300000);

            // SELECT文を発行し結果を表示する
            List<Employee> result1 = sqlSession.selectList("selectEmployeesWithParam",
                    param1);
            showEntityList(result1);

            // パラメータとなるEmployeeを生成する
            Employee param2 = new Employee(null, null, "PLANNING", 470000);

            // SELECT文を発行し結果を表示する
            // Employeeをリストで受け取るメソッドの場合、ヒットしない場合は空のリストが返される
            List<Employee> result2 = sqlSession.selectList("selectEmployeesWithParam",
                    param2);
            System.out.println(result2 != null);
            System.out.println(result2.size());
        }
    }
}
