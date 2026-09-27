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
                    + "DEPARTMENT_ID INTEGER, EMPLOYEE_NAME VARCHAR(100) NOT NULL, "
                    + "SALARY DECIMAL(12, 2) NOT NULL)");
            // 初期データ16人（部署ID 1=PLANNING / 2=HR / 3=SALES / 4=PRODUCT、Victorは未所属）
            statement.executeUpdate("INSERT INTO EMPLOYEE VALUES "
                    + "(10001, 3, 'Alice', 500000), "
                    + "(10002, 1, 'Bob', 450000), "
                    + "(10003, 2, 'Carol', 350000), "
                    + "(10004, 3, 'Dave', 400000), "
                    + "(10005, 3, 'Ellen', 300000), "
                    + "(10006, 1, 'Frank', 250000), "
                    + "(10007, 4, 'Ivan', 480000), "
                    + "(10008, 2, 'Justin', 460000), "
                    + "(10009, 4, 'Mallory', 420000), "
                    + "(10010, 3, 'Matilda', 280000), "
                    + "(10011, 4, 'Oscar', 320000), "
                    + "(10012, 4, 'Pat', 240000), "
                    + "(10013, 3, 'Peggy', 270000), "
                    + "(10014, NULL, 'Victor', 220000), "
                    + "(10015, 1, 'Steve', 380000), "
                    + "(10016, 4, 'Trent', 310000)");
        }
        factory = MyBatisFactory.create();
    }

    // 「CRUDと明示的コミット」の検証
    @Test
    void performsCrudAndExplicitCommit() {
        try (SqlSession session = factory.openSession()) {
            EmployeeMapper mapper = session.getMapper(EmployeeMapper.class);
            Employee employee = new Employee(10017, 3, "Walter", new BigDecimal("230000"));
            assertEquals(1, mapper.insert(employee));
            session.commit();
        }

        try (SqlSession session = factory.openSession()) {
            EmployeeMapper mapper = session.getMapper(EmployeeMapper.class);
            Employee found = mapper.findById(10017);
            assertEquals("Walter", found.getName());
            found.setSalary(new BigDecimal("260000"));
            assertEquals(1, mapper.update(found));
            assertEquals(1, mapper.delete(10017));
            session.commit();
            assertNull(mapper.findById(10017));
        }
    }
}
