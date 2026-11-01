package pro.kensait.jdbc.company;

import static org.junit.jupiter.api.Assertions.*;
import static pro.kensait.jdbc.util.DatabaseUtil.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.github.database.rider.junit5.api.DBRider;

/*
 * チャプター4.1 と同じ EmployeeDAO のテストを、アノテーションと YAML で書いたテストクラス
 */
@DBRider                         // 【1】
@DataSet("datasets/init.yml")    // 【2】
public class EmployeeDAOTest {
    Connection conn;
    EmployeeDAO employeeDAO;

    @BeforeEach
    void setUp() throws Exception {
        String url = getProperty("jdbc.url");
        String user = getProperty("jdbc.user");
        String password = getProperty("jdbc.password");
        conn = DriverManager.getConnection(url, user, password); // 【3】
        employeeDAO = new EmployeeDAO(conn);
    }

    @AfterEach
    void tearDown() throws Exception {
        conn.close();
    }

    @Test
    @DisplayName("主キー検索の結果をテストする")
    void test_SelectEmployee() {
        Employee actual = employeeDAO.selectEmployee(10001);              // 【1】
        assertEquals(new Employee(10001, "Alice", "SALES",
                LocalDate.of(2015, 4, 1), "MANAGER", 500000), actual);
    }

    @Test
    @ExpectedDataSet("datasets/expected_update.yml")                     // 【2】
    @DisplayName("一括更新の結果をテストする")
    void test_UpdateSalary() {
        employeeDAO.updateEmployeeSalary("SALES", 3000);                 // 【3】
    }

    @Test
    @ExpectedDataSet(value = "datasets/expected_insert.yml", orderBy = "EMPLOYEE_ID")
    @DisplayName("挿入の結果をテストする（並び順をそろえて比べる）")
    void test_InsertEmployee() {
        employeeDAO.insertEmployee(new Employee(10006, "Frank", "SALES",
                LocalDate.of(2017, 10, 1), null, 380000));
    }
}
