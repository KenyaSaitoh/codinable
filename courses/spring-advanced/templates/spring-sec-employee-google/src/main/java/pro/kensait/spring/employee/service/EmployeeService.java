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
    private int sequence = 10017;
    // 社員の初期化（16件）
    public EmployeeService() {
        employees.put(10001, new Employee(10001, "Alice", 3, "SALES", "MANAGER",
                500000, LocalDate.of(2012, 4, 1)));
        employees.put(10002, new Employee(10002, "Bob", 1, "PLANNING", "MANAGER",
                450000, LocalDate.of(2012, 4, 1)));
        employees.put(10003, new Employee(10003, "Carol", 2, "HR", "CHIEF",
                350000, LocalDate.of(2012, 4, 1)));
        employees.put(10004, new Employee(10004, "Dave", 3, "SALES", "LEADER",
                400000, LocalDate.of(2012, 4, 1)));
        employees.put(10005, new Employee(10005, "Ellen", 3, "SALES", "CHIEF",
                300000, LocalDate.of(2013, 4, 1)));
        employees.put(10006, new Employee(10006, "Frank", 1, "PLANNING", "ASSOCIATE",
                250000, LocalDate.of(2013, 10, 1)));
        employees.put(10007, new Employee(10007, "Ivan", 4, "PRODUCT", "MANAGER",
                480000, LocalDate.of(2014, 1, 1)));
        employees.put(10008, new Employee(10008, "Justin", 2, "HR", "MANAGER",
                460000, LocalDate.of(2014, 4, 1)));
        employees.put(10009, new Employee(10009, "Mallory", 4, "PRODUCT", "LEADER",
                420000, LocalDate.of(2014, 7, 1)));
        employees.put(10010, new Employee(10010, "Matilda", 3, "SALES", "ASSOCIATE",
                280000, LocalDate.of(2015, 8, 1)));
        employees.put(10011, new Employee(10011, "Oscar", 4, "PRODUCT", "CHIEF",
                320000, LocalDate.of(2015, 11, 1)));
        employees.put(10012, new Employee(10012, "Pat", 4, "PRODUCT", "ASSOCIATE",
                240000, LocalDate.of(2016, 4, 1)));
        employees.put(10013, new Employee(10013, "Peggy", 3, "SALES", "ASSOCIATE",
                270000, LocalDate.of(2016, 10, 1)));
        employees.put(10014, new Employee(10014, "Victor", null, null, "ASSOCIATE",
                220000, LocalDate.of(2017, 4, 1)));
        employees.put(10015, new Employee(10015, "Steve", 1, "PLANNING", "LEADER",
                380000, LocalDate.of(2017, 10, 1)));
        employees.put(10016, new Employee(10016, "Trent", 4, "PRODUCT", "CHIEF",
                310000, LocalDate.of(2017, 10, 1)));
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
