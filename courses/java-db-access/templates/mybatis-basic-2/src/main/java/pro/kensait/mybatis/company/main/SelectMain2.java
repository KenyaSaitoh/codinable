package pro.kensait.mybatis.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.Employee;
import pro.kensait.mybatis.company.mapper.EmployeeMapper;
/*
 * SELECT文、複数条件検索（IN句展開）
 */
public class SelectMain2 {

    public static void main(String[] args) {
        // SqlSessionを取得する
        try (SqlSession sqlSession = SqlSessionHolder.getInstance().getSqlSession()) {

            // Mapperを取得する
            EmployeeMapper mapper = sqlSession.getMapper(EmployeeMapper.class);

            // パラメータとなるリストを生成する
            List<String> departmentNameList = new ArrayList<String>();
            departmentNameList.add("PLANNING");
            departmentNameList.add("PRODUCT");

            // SELECT文を発行し結果を表示する
            List<Employee> resultList = mapper.selectVariousDepartment(departmentNameList);
            showEntityList(resultList);
        }
    }
}
