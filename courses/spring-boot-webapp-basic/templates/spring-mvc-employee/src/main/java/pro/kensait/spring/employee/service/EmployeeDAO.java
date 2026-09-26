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

    // 社員daoの初期化
    public EmployeeDAO() {
        employees.put(1, new Employee(1, "山田 太郎", "営業部", 320_000));
        employees.put(2, new Employee(2, "佐藤 花子", "開発部", 450_000));
        employees.put(3, new Employee(3, "鈴木 一郎", "人事部", 300_000));
        employees.put(4, new Employee(4, "高橋 美咲", "総務部", 380_000));
        employees.put(5, new Employee(5, "田中 健太", "営業部", 280_000));
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
