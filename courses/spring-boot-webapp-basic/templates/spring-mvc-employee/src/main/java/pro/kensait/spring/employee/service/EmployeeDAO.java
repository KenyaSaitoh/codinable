package pro.kensait.spring.employee.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;
/** インメモリで社員を保持する簡易DAO */
@Repository
public class EmployeeDAO {
    private final Map<Integer, Employee> employees = new ConcurrentHashMap<>();

    // 社員daoの初期化（Victor は部署なし）
    public EmployeeDAO() {
        employees.put(10001, new Employee(10001, "Alice", "SALES", 500_000));
        employees.put(10002, new Employee(10002, "Bob", "PLANNING", 450_000));
        employees.put(10003, new Employee(10003, "Carol", "HR", 350_000));
        employees.put(10004, new Employee(10004, "Dave", "SALES", 400_000));
        employees.put(10005, new Employee(10005, "Ellen", "SALES", 300_000));
        employees.put(10006, new Employee(10006, "Frank", "PLANNING", 250_000));
        employees.put(10007, new Employee(10007, "Ivan", "PRODUCT", 480_000));
        employees.put(10008, new Employee(10008, "Justin", "HR", 460_000));
        employees.put(10009, new Employee(10009, "Mallory", "PRODUCT", 420_000));
        employees.put(10010, new Employee(10010, "Matilda", "SALES", 280_000));
        employees.put(10011, new Employee(10011, "Oscar", "PRODUCT", 320_000));
        employees.put(10012, new Employee(10012, "Pat", "PRODUCT", 240_000));
        employees.put(10013, new Employee(10013, "Peggy", "SALES", 270_000));
        employees.put(10014, new Employee(10014, "Victor", null, 220_000));
        employees.put(10015, new Employee(10015, "Steve", "PLANNING", 380_000));
        employees.put(10016, new Employee(10016, "Trent", "PRODUCT", 310_000));
    }

    // 主キー検索
    public Employee find(int employeeId) {
        return employees.get(employeeId);
    }

    // 全件検索
    public List<Employee> findAll() {
        return employees.values().stream()
                .sorted(Comparator.comparing(Employee::getEmployeeId)).toList();
    }

    // 下限月給の検索
    public List<Employee> findByLowerSalary(int lowerSalary) {
        return findAll().stream()
                .filter(employee -> lowerSalary <= employee.getSalary()).toList();
    }

    // 最大社員IDの取得
    public int getMaxEmployeeId() {
        return employees.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    // 挿入
    public int save(Employee employee) {
        employees.put(employee.getEmployeeId(), employee);
        return employee.getEmployeeId();
    }

    // APIメソッド：Employeeの削除
    public int delete(int employeeId) {
        return employees.remove(employeeId) == null ? 0 : 1;
    }

    // 一件更新
    public int update(Employee employee) {
        if (!employees.containsKey(employee.getEmployeeId())) {
            return 0;
        }
        employees.put(employee.getEmployeeId(), employee);
        return 1;
    }
}
