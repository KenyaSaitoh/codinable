package pro.kensait.mybatis.company;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import pro.kensait.course.SampleDatabase;
import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.Employee;
import pro.kensait.mybatis.company.dto.EmployeeDynamicParam;
import pro.kensait.mybatis.company.mapper.EmployeeMapper;

/*
 * dynamicSQLのテスト
 */
class DynamicSqlTest {
    // 「任意条件の組み合わせと値のバインド」の検証
    @Test
    void combinesOptionalConditionsAndBindsValues() throws Exception {
        SampleDatabase.reset();
        try (var session = SqlSessionHolder.getInstance().getSqlSession()) {
            var mapper = session.getMapper(EmployeeMapper.class);
            assertEquals(14, mapper.selectDynamicEmployees(
                    new EmployeeDynamicParam(null, null, null)).size());
            var employees = mapper.selectDynamicEmployees(
                    new EmployeeDynamicParam("SALES", 300000, 400000));
            assertEquals(List.of(10004, 10005), employees.stream()
                    .map(Employee::getEmployeeId).sorted().toList());
            assertTrue(mapper.selectDynamicEmployees(
                    new EmployeeDynamicParam("' OR '1'='1", null, null)).isEmpty());
        }
    }
}
