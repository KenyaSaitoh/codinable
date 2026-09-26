package pro.kensait.db.mybatisdynamic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Reader;
import java.math.BigDecimal;
import java.sql.DriverManager;
import java.util.List;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/*
 * companyマッパーのテスト
 */
class CompanyMapperTest {
    private SqlSessionFactory factory;

    // 各テストケースで共通的な前処理
    @BeforeEach
    void setUp() throws Exception {
        try (var connection = DriverManager.getConnection(
                "jdbc:hsqldb:mem:mybatis_dynamic", "SA", "");
                var statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE EMPLOYEE IF EXISTS");
            statement.executeUpdate("DROP TABLE DEPARTMENT IF EXISTS");
            statement.executeUpdate("CREATE TABLE DEPARTMENT (DEPARTMENT_ID INTEGER PRIMARY KEY, "
                    + "DEPARTMENT_NAME VARCHAR(100) NOT NULL)");
            statement.executeUpdate("CREATE TABLE EMPLOYEE (EMPLOYEE_ID INTEGER PRIMARY KEY, "
                    + "DEPARTMENT_ID INTEGER NOT NULL REFERENCES DEPARTMENT, "
                    + "EMPLOYEE_NAME VARCHAR(100) NOT NULL, SALARY DECIMAL(12, 2) NOT NULL)");
            statement.executeUpdate("INSERT INTO DEPARTMENT VALUES (10, '営業部'), (20, '開発部')");
            statement.executeUpdate("INSERT INTO EMPLOYEE VALUES "
                    + "(101, 10, '佐藤 花子', 420000), (102, 10, '鈴木 一郎', 380000), "
                    + "(201, 20, '田中 次郎', 520000)");
        }
        try (Reader reader = Resources.getResourceAsReader("mybatis-config.xml")) {
            factory = new SqlSessionFactoryBuilder().build(reader);
        }
    }

    // 「動的WHERE句の構築と集計結果のマッピング」の検証
    @Test
    void buildsDynamicWhereAndMapsAggregates() {
        try (SqlSession session = factory.openSession()) {
            CompanyMapper mapper = session.getMapper(CompanyMapper.class);
            EmployeeFilter filter = new EmployeeFilter(10, new BigDecimal("400000"),
                    List.of(101, 102, 201));
            List<EmployeeView> employees = mapper.search(filter);
            assertEquals(List.of(101), employees.stream().map(EmployeeView::id).toList());
            assertEquals(2, mapper.summarizeDepartments().getFirst().employeeCount());
        }
    }
}
