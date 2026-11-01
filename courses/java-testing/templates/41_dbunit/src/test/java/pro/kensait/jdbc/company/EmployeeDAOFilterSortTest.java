package pro.kensait.jdbc.company;

import static org.dbunit.Assertion.*; // これを先にimportすることが重要！
import static pro.kensait.jdbc.util.DatabaseUtil.*;

import java.io.File;
import java.time.LocalDate;

import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("EmployeeDAOを対象にしたテストクラス（挿入後にソートして検証）")
public class EmployeeDAOFilterSortTest {
    // DBUnitのリソースを格納するディレクトリ
    private static final String INIT_DATA_DIR = "src/test/resources/INIT_DATA";
    private static final String EXPECTED_DATA_DIR_1 = "src/test/resources/EXPECTED_DATA_1";

    // テスト対象クラス
    EmployeeDAO employeeDAO;

    // DBUnitのためのフィクスチャ
    IDatabaseTester databaseTester;
    IDatabaseConnection databaseConnection;

    /*
     *  DBUnitのテストフィクスチャやデータベース上のデータを初期化する
     */
    @BeforeEach
    public void setUpDatabase() throws Exception {
        // プロパティファイルよりデータベース情報を取得する
        String driver = getProperty("jdbc.driver");
        String url = getProperty("jdbc.url");
        String user = getProperty("jdbc.user");
        String password = getProperty("jdbc.password");

        // IDatabaseTesterを初期化する
        databaseTester = new JdbcDatabaseTester(driver, url, user, password);

        // IDatabaseConnectionを取得する
        databaseConnection = databaseTester.getConnection();

        // HSQLDB用のDataTypeFactoryを設定する
        DatabaseConfig config = databaseConnection.getConfig();
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());

        // IDatabaseConnectionからJDBCコネクションを取り出し、EmployeeDAOを初期化する
        employeeDAO = new EmployeeDAO(databaseConnection.getConnection());

        // 初期データをセットアップする
        initData();
    }

    /*
     * 初期データをセットアップする
     */
    private void initData() throws Exception {
        // CSVファイルから初期データを読み込む
        IDataSet dataSet = new CsvDataSet(new File(INIT_DATA_DIR));

        // データベースの対象テーブルから全件削除し、読み込んだ初期データを挿入する
        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        databaseTester.onSetup();
    }

    @Test
    @DisplayName("挿入の結果をテストする（ソートして全件検証）")
    void test_InsertEmployee() throws Exception {
        // テストを実行する
        Employee employee = new Employee(10006, "Frank", "SALES", LocalDate.of(2017, 10, 1),
                null, 380000);
        employeeDAO.insertEmployee(employee);

        String[] excludedColumns = {"ENTRANCE_DATE"};                          // 【1】
        IDataSet expectedDataSet = new CsvDataSet(new File(EXPECTED_DATA_DIR_1));
        ITable expectedTable = DefaultColumnFilter.excludedColumnsTable(
                expectedDataSet.getTable("EMPLOYEE"), excludedColumns);       // 【2】
        IDataSet databaseDataSet = databaseTester.getConnection().createDataSet();
        ITable actualTable = DefaultColumnFilter.excludedColumnsTable(
                databaseDataSet.getTable("EMPLOYEE"), excludedColumns);
        ITable sortedActualTable = new SortedTable(actualTable, new String[]{"EMPLOYEE_ID"}); // 【3】
        assertEquals(expectedTable, sortedActualTable);
    }
}