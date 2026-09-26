package pro.kensait.db.mybatis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
 * 社員マッパーのテスト
 */
class EmployeeMapperTest {
    private SqlSessionFactory factory;

    // 各テストケースで共通的な前処理
    @BeforeEach
    void setUp() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:hsqldb:mem:mybatis_crud", "SA", "");
                Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE EMPLOYEE IF EXISTS");
            statement.executeUpdate("CREATE TABLE EMPLOYEE (EMPLOYEE_ID INTEGER PRIMARY KEY, "
                    + "DEPARTMENT_ID INTEGER NOT NULL, EMPLOYEE_NAME VARCHAR(100) NOT NULL, "
                    + "SALARY DECIMAL(12, 2) NOT NULL)");
        }
        factory = MyBatisFactory.create();
    }

    // 「CRUDと明示的コミット」の検証
    @Test
    void performsCrudAndExplicitCommit() {
        try (SqlSession session = factory.openSession()) {
            EmployeeMapper mapper = session.getMapper(EmployeeMapper.class);
            Employee employee = new Employee(101, 10, "Alice", new BigDecimal("420000"));
            assertEquals(1, mapper.insert(employee));
            session.commit();
        }

        try (SqlSession session = factory.openSession()) {
            EmployeeMapper mapper = session.getMapper(EmployeeMapper.class);
            Employee found = mapper.findById(101);
            assertEquals("Alice", found.getName());
            found.setSalary(new BigDecimal("450000"));
            assertEquals(1, mapper.update(found));
            assertEquals(1, mapper.delete(101));
            session.commit();
            assertNull(mapper.findById(101));
        }
    }
}
