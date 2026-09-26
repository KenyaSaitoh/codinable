package pro.kensait.mybatis.company;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import pro.kensait.course.SampleDatabase;
import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.Employee;
import pro.kensait.mybatis.company.mapper.EmployeeMapper;
import pro.kensait.mybatis.company.type.JobType;

/*
 * generatedキーのテスト
 */
class GeneratedKeyTest {
    // 「空の部署一覧による社員未検出」の検証
    @Test
    void emptyDepartmentListMatchesNoEmployees() throws Exception {
        SampleDatabase.reset();
        try (var session = SqlSessionHolder.getInstance().getSqlSession()) {
            assertTrue(session.getMapper(EmployeeMapper.class)
                    .selectVariousDepartment(List.of()).isEmpty());
        }
    }

    // 「自動採番キーの返却と行のコミット」の検証
    @Test
    void returnsTheGeneratedKeyAndCommitsTheRow() throws Exception {
        SampleDatabase.reset();
        var holder = SqlSessionHolder.getInstance();
        var employee = new Employee(null, "Generated", "SALES", LocalDate.of(2026, 1, 1),
                JobType.CHIEF, 380000, 0L);
        try (var session = holder.getSqlSession()) {
            session.getMapper(EmployeeMapper.class).insertEmployeeWithKeyGen(employee);
            assertNotNull(employee.getEmployeeId());
            session.commit();
        }
        try (var session = holder.getSqlSession()) {
            assertEquals("Generated", session.getMapper(EmployeeMapper.class)
                    .selectEmployee(employee.getEmployeeId()).getEmployeeName());
        }
    }
}
