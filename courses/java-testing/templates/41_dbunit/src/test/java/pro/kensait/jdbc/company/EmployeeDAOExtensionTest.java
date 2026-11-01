package pro.kensait.jdbc.company;

import static org.dbunit.Assertion.assertEquals; // これを先にimportすることが重要！
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static pro.kensait.jdbc.util.DatabaseUtil.*;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;

import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.junit.jupiter.DbUnitExtension;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/*
 * EmployeeDAOTest と同じ初期化を、DbUnitExtension に任せるテストクラス
 * onSetup() は書かない。拡張機能が @BeforeEach の後、テストメソッドの直前に呼び出す
 */
@ExtendWith(DbUnitExtension.class) // 【1】
@DisplayName("EmployeeDAOを対象にしたテストクラス（DbUnitExtension）")
public class EmployeeDAOExtensionTest {
    private static final String INIT_DATA_DIR = "src/test/resources/INIT_DATA";
    private static final String EXPECTED_DATA_DIR_3 = "src/test/resources/EXPECTED_DATA_3";

    IDatabaseTester databaseTester; // 【2】
    Connection conn;
    EmployeeDAO employeeDAO;

    @BeforeEach
    void setUpDatabase() throws Exception {
        String driver = getProperty("jdbc.driver");
        String url = getProperty("jdbc.url");
        String user = getProperty("jdbc.user");
        String password = getProperty("jdbc.password");

        databaseTester = new JdbcDatabaseTester(driver, url, user, password);
        databaseTester.setDataSet(new CsvDataSet(new File(INIT_DATA_DIR)));
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        // onSetup()は呼び出さない【3】
        conn = DriverManager.getConnection(url, user, password);
        employeeDAO = new EmployeeDAO(conn);
    }

    @AfterEach
    void tearDown() throws Exception {
        conn.close();
    }

    @Test
    @DisplayName("主キー検索の結果をテストする")
    void test_SelectEmployee() {
        Employee actual = employeeDAO.selectEmployee(10001);
        assertEquals(new Employee(10001, "Alice", "SALES", LocalDate.of(2015, 4, 1),
                "MANAGER", 500000), actual);
    }

    @Test
    @DisplayName("一括更新の結果をテストする")
    void test_UpdateSalary() throws Exception {
        employeeDAO.updateEmployeeSalary("SALES", 3000);
        ITable expectedTable = new CsvDataSet(new File(EXPECTED_DATA_DIR_3)).getTable("EMPLOYEE");
        ITable actualTable = databaseTester.getConnection().createDataSet().getTable("EMPLOYEE");
        assertEquals(expectedTable, actualTable);
    }
}
