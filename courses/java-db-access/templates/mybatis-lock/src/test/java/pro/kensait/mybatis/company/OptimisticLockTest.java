package pro.kensait.mybatis.company;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import pro.kensait.course.SampleDatabase;
import pro.kensait.mybatis.company.common.SqlSessionHolder;
import pro.kensait.mybatis.company.dto.Employee;
import pro.kensait.mybatis.company.mapper.EmployeeMapper;

/*
 * optimisticlockのテスト
 */
class OptimisticLockTest {
    // 「古いバージョンによる更新0件と月給の上書き防止」の検証
    @Test
    void staleVersionUpdatesZeroRowsAndDoesNotOverwriteSalary() throws Exception {
        SampleDatabase.reset();
        var holder = SqlSessionHolder.getInstance();
        Employee stale;
        try (var session = holder.getSqlSession()) {
            stale = session.getMapper(EmployeeMapper.class).selectEmployee(10001);
        }
        try (var session = holder.getSqlSession()) {
            var mapper = session.getMapper(EmployeeMapper.class);
            var winner = mapper.selectEmployee(10001);
            winner.setSalary(510000);
            assertEquals(1, mapper.updateEmployeeWithOptimisticLock(winner));
            session.commit();
        }
        try (var session = holder.getSqlSession()) {
            stale.setSalary(999999);
            assertEquals(0, session.getMapper(EmployeeMapper.class)
                    .updateEmployeeWithOptimisticLock(stale));
            session.rollback();
        }
        try (var session = holder.getSqlSession()) {
            assertEquals(510000, session.getMapper(EmployeeMapper.class)
                    .selectEmployee(10001).getSalary());
        }
    }
}
