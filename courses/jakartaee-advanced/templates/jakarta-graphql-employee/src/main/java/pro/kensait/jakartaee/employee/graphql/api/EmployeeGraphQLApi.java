package pro.kensait.jakartaee.employee.graphql.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;
import pro.kensait.jakartaee.employee.graphql.entity.Department;
import pro.kensait.jakartaee.employee.graphql.entity.Employee;
import pro.kensait.jakartaee.employee.graphql.service.DepartmentService;
import pro.kensait.jakartaee.employee.graphql.service.EmployeeNotFoundException;
import pro.kensait.jakartaee.employee.graphql.service.EmployeeService;
import pro.kensait.jakartaee.employee.graphql.service.InvalidEmployeeException;

/*
 * GraphQL の入口（http://localhost:8080/jakarta-graphql-employee/graphql）
 * このクラスのメソッドから、スキーマの Query・Mutation と Department.employees が生成される
 * 業務の処理とデータアクセスはサービスへ任せる
 */
@GraphQLApi // 【1】
@ApplicationScoped
public class EmployeeGraphQLApi {
    @Inject
    private EmployeeService employeeService;
    @Inject
    private DepartmentService departmentService;

    @Query("employee") // 【2】
    @Description("社員IDで社員を1件取得する")
    public Employee employee(@Name("id") @Id int id)
            throws EmployeeNotFoundException { // 【3】
        return employeeService.getEmployee(id); // 【4】
    }

    @Query("employees")
    @NonNull
    public List<@NonNull Employee> employees() { // 【5】
        return employeeService.getEmployeesAll();
    }

    @Query("employeesByDepartment")
    @Description("部署IDで所属社員を検索する")
    @NonNull
    public List<@NonNull Employee> employeesByDepartment(@Name("departmentId") @Id int departmentId) {
        return employeeService.getEmployeesByDepartment(departmentId);
    }

    @Query("departments")
    @NonNull
    public List<@NonNull Department> departments() {
        return departmentService.getDepartmentsAll();
    }

    // 部署ごとに呼ばれる版（N+1 問題の説明用）。下のバッチ版と入れ替えると、
    // departments { employees { ... } } で getEmployeesByDepartment が部署の数だけ呼ばれる
    // public List<Employee> employees(@Source Department department) { // 【1】
    //     return employeeService.getEmployeesByDepartment(department.getDepartmentId()); // 【2】
    // }

    // SmallRye GraphQL の機能: 同じ階層の部署をまとめて受け取り、1 回で解決する
    public List<List<Employee>> employees(@Source List<Department> departments) { // 【1】
        return employeeService.employeesByDepartments(departments); // 【2】
    }

    @Mutation("createEmployee")
    @NonNull
    public Employee createEmployee(@Name("input") @NonNull EmployeeInput input)
            throws InvalidEmployeeException {
        return employeeService.createEmployee(input);
    }

    @Mutation("updateEmployee")
    @NonNull
    public Employee updateEmployee(@Name("id") @Id int id, @Name("input") @NonNull EmployeeInput input)
            throws EmployeeNotFoundException, InvalidEmployeeException {
        return employeeService.updateEmployee(id, input);
    }

    @Mutation("deleteEmployee")
    @Id
    @NonNull
    public Integer deleteEmployee(@Name("id") @Id int id) throws EmployeeNotFoundException {
        return employeeService.deleteEmployee(id);
    }
}
