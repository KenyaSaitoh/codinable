package pro.kensait.mybatis.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.Employee;
import pro.kensait.mybatis.company.dto.EmployeeDynamicParam;
import pro.kensait.mybatis.company.mapper.EmployeeMapper;
/*
 * 動的クエリ(1)
 */
public class DynamicSelectMain1 {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // Mapperを取得する
            EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);

            // パラメータとなるEmployeeDynamicParamインスタンスを生成する
            EmployeeDynamicParam paramEmployee =
                    new EmployeeDynamicParam("SALES", 250000, 300000);

            // SELECT文を発行し結果を表示する
            List<Employee> resultList = mapper.selectDynamicEmployees(paramEmployee);
            showEntityList(resultList);
        }
    }
}
