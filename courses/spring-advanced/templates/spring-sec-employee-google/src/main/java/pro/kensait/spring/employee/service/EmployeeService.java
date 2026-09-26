package pro.kensait.spring.employee.service;
import java.time.LocalDate;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
/*
 * 社員管理の業務ロジック
 */
@Service
public class EmployeeService {
    private final Map<Integer, Employee> employees = new LinkedHashMap<>();
    private int sequence = 3;
    // 社員の初期化
    public EmployeeService() {
        employees.put(1, new Employee(1, "Alice", 10, "営業部", "Sales",
                300000, LocalDate.of(2018, 4, 1)));
        employees.put(2, new Employee(2, "Bob", 20, "開発部", "Engineer",
                350000, LocalDate.of(2015, 4, 1)));
    }
    // 全件検索
    public synchronized List<Employee> findAll() {
        return List.copyOf(employees.values());
    }
    // 主キー検索
    public synchronized Employee find(int id) {
        Employee employee = employees.get(id);
        if (employee == null) {
            throw new java.util.NoSuchElementException("社員が存在しません");
        }
        return employee;
    }
    // データの保存
    public synchronized Employee save(Integer id, Employee input) {
        if (id != null) {
            find(id);
        }
        int employeeId = id == null ? sequence++ : id;
        Employee result = new Employee(employeeId, input.employeeName(),
                input.departmentId(), input.departmentName(), input.jobName(),
                input.salary(), input.entranceDate());
        employees.put(employeeId, result);
        return result;
    }
    // 指定した社員を論理削除
    public synchronized void delete(int id) {
        find(id);
        employees.remove(id);
    }
}
