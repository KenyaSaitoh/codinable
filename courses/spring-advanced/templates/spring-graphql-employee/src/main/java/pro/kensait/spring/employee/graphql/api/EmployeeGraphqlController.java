package pro.kensait.spring.employee.graphql.api;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import jakarta.validation.Valid;
import pro.kensait.spring.employee.graphql.entity.Department;
import pro.kensait.spring.employee.graphql.entity.Employee;
import pro.kensait.spring.employee.graphql.service.DepartmentService;
import pro.kensait.spring.employee.graphql.service.EmployeeService;

/*
 * 社員管理システムのGraphQL APIのコントローラーを担うクラス
 * （RESTと異なり@RestControllerではなく@Controllerを付与し、
 * スキーマのQuery/Mutationの各フィールドをメソッドにマッピングする）
 */
@Controller
public class EmployeeGraphqlController {
    private static final Logger logger = LoggerFactory.getLogger(
            EmployeeGraphqlController.class);

    // インジェクションポイント
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DepartmentService departmentService;

    // クエリメソッド：主キー検索によるEmployee取得
    // （@QueryMappingにより、スキーマの「Query.employee」にマッピングされる
    // 引数は@Argumentによりスキーマの引数「id」からバインドされる）
    @QueryMapping
    public Employee employee(@Argument("id") Integer id) {
        logger.info("[ EmployeeGraphqlController#employee ]");

        // ビジネスロジックを呼び出し、Employeeエンティティを取得する
        // 対象が存在しない場合はEmployeeNotFoundExceptionが送出され、
        // レスポンスの「errors」配列としてクライアントに返される
        return employeeService.getEmployee(id);
    }

    // クエリメソッド：全Employeeリスト取得
    @QueryMapping
    public List<Employee> employees() {
        logger.info("[ EmployeeGraphqlController#employees ]");

        // ビジネスロジックを呼び出し、Employeeエンティティのリストを取得する
        // どのフィールドを返すかはクライアントのクエリが決める（スキーマ駆動）
        return employeeService.getEmployeesAll();
    }

    // クエリメソッド：条件検索（部署IDをキーに）によるEmployeeリスト取得
    @QueryMapping
    public List<Employee> employeesByDepartment(
            @Argument("departmentId") Integer departmentId) {
        logger.info("[ EmployeeGraphqlController#employeesByDepartment ]");

        // ビジネスロジックを呼び出し、Employeeエンティティのリストを取得する
        return employeeService.getEmployeesByDepartment(departmentId);
    }

    // クエリメソッド：全Departmentリスト取得
    @QueryMapping
    public List<Department> departments() {
        logger.info("[ EmployeeGraphqlController#departments ]");

        // ビジネスロジックを呼び出し、Departmentエンティティのリストを取得する
        return departmentService.getDepartmentsAll();
    }

    // ミューテーションメソッド：Employeeの新規作成
    // （@MutationMappingにより、スキーマの「Mutation.createEmployee」にマッピングされる
    // 「input EmployeeInput」はEmployeeInputレコードに自動的にバインドされ、
    // Bean Validationによる入力値検証も適用される）
    @MutationMapping
    public Employee createEmployee(@Argument("input") @Valid EmployeeInput input) {
        logger.info("[ EmployeeGraphqlController#createEmployee ]");

        // ビジネスロジックを呼び出し、Employeeを新規作成する
        // このとき、DBで新規採番されたIDを含むEmployeeエンティティを取得する
        return employeeService.createEmployee(
                input.employeeName(),
                input.departmentId(),
                input.jobName(),
                input.salary(),
                toLocalDate(input.entranceDate()));
    }

    // ミューテーションメソッド：Employeeの更新
    @MutationMapping
    public Employee updateEmployee(@Argument("id") Integer id,
            @Argument("input") @Valid EmployeeInput input) {
        logger.info("[ EmployeeGraphqlController#updateEmployee ]");

        // 引数の社員IDをキーに、ビジネスロジックを呼び出してEmployeeを更新する
        return employeeService.updateEmployee(
                id,
                input.employeeName(),
                input.departmentId(),
                input.jobName(),
                input.salary(),
                toLocalDate(input.entranceDate()));
    }

    // ミューテーションメソッド：Employeeの削除
    @MutationMapping
    public Integer deleteEmployee(@Argument("id") Integer id) {
        logger.info("[ EmployeeGraphqlController#deleteEmployee ]");

        // ビジネスロジックを呼び出し、Employeeを削除する
        employeeService.removeEmployee(id);

        // 削除したEmployeeのIDを返す
        return id;
    }

    // スキーママッピング：Employee.idフィールド
    // （エンティティのプロパティ名はemployeeIdでスキーマのフィールド名idと一致しないため、
    // @SchemaMappingで明示的に対応付ける）
    @SchemaMapping(typeName = "Employee", field = "id")
    public Integer employeeId(Employee employee) {
        return employee.getEmployeeId();
    }

    // スキーママッピング：Employee.entranceDateフィールド
    // （GraphQLの標準スカラー型には日付型がないため、LocalDateを文字列に変換して返す
    // 本格的にはgraphql-java-extended-scalarsでカスタムスカラー「Date」を定義する方法もある）
    @SchemaMapping(typeName = "Employee", field = "entranceDate")
    public String entranceDate(Employee employee) {
        return employee.getEntranceDate() == null ? null
                : employee.getEntranceDate().toString();
    }

    // スキーママッピング：Department.idフィールド
    @SchemaMapping(typeName = "Department", field = "id")
    public Integer departmentId(Department department) {
        return department.getDepartmentId();
    }

    // 部署ごとのクエリを一括化してN+1を防ぐ空の部署にも空リストの返却
    @BatchMapping(typeName = "Department", field = "employees")
    public java.util.Map<Department, List<Employee>> employees(List<Department> departments) {
        return employeeService.employeesByDepartments(departments);
    }

    // 入社日の文字列（"yyyy-MM-dd"形式）をLocalDateへの変換
    private LocalDate toLocalDate(String entranceDate) {
        return entranceDate == null ? null : LocalDate.parse(entranceDate);
    }
}
