package pro.kensait.jakartaee.employee.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * 社員をメモリー上の Map で保持する DAO（データアクセス層）
 * @ApplicationScoped なので、すべてのリクエストが同じ Map を共有する
 * アプリケーションを停止（デプロイし直し）すると、初期データに戻る
 */
@ApplicationScoped
public class EmployeeDAO {
    private final Map<Integer, Employee> employees = new ConcurrentHashMap<>();

    // 初期データの登録。コンテナが依存先を注入した後に一度だけ呼ぶ（Victor は部署なし）
    @PostConstruct
    public void init() {
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

    public Employee find(int employeeId) {
        return employees.get(employeeId);
    }

    public List<Employee> findAll() {
        return employees.values().stream()
                .sorted(Comparator.comparing(Employee::getEmployeeId)).toList();
    }

    public int getMaxEmployeeId() {
        return employees.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    public int save(Employee employee) {
        employees.put(employee.getEmployeeId(), employee);
        return employee.getEmployeeId();
    }

    public int update(Employee employee) {
        if (!employees.containsKey(employee.getEmployeeId())) {
            return 0;
        }
        employees.put(employee.getEmployeeId(), employee);
        return 1;
    }

    public int delete(int employeeId) {
        return employees.remove(employeeId) == null ? 0 : 1;
    }
}
