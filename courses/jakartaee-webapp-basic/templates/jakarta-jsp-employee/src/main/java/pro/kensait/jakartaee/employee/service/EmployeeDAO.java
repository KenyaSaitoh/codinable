package pro.kensait.jakartaee.employee.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 社員をメモリー上の Map で保持する DAO
 * アプリケーション全体で 1 つを共有し、同時に呼ばれるので ConcurrentHashMap を使う
 * アプリケーションを停止すると、削除した社員も元に戻る
 */
public class EmployeeDAO {
    private final Map<Integer, Employee> employees = new ConcurrentHashMap<>();

    // 初期データ（Victor は部署なし）
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

    public List<Employee> findAll() {
        return employees.values().stream()
                .sorted(Comparator.comparing(Employee::getEmployeeId)).toList();
    }

    public int delete(int employeeId) {
        return employees.remove(employeeId) == null ? 0 : 1;
    }
}
