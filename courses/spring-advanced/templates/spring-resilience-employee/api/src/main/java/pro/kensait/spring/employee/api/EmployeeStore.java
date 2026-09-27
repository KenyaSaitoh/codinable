package pro.kensait.spring.employee.api;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** プロセス内だけで保持する教材用データ再起動すると初期状態に戻る */
final class EmployeeStore {
    private final Map<Integer, Employee> employees = new TreeMap<>();
    private int nextId = 10017;

    // 社員storeの初期化（16件。Victorは部署なし）
    public EmployeeStore() {
        add(new Employee(10001, "Alice", 3, "SALES", "MANAGER", 500000,
                LocalDate.of(2012, 4, 1)));
        add(new Employee(10002, "Bob", 1, "PLANNING", "MANAGER", 450000,
                LocalDate.of(2012, 4, 1)));
        add(new Employee(10003, "Carol", 2, "HR", "CHIEF", 350000,
                LocalDate.of(2012, 4, 1)));
        add(new Employee(10004, "Dave", 3, "SALES", "LEADER", 400000,
                LocalDate.of(2012, 4, 1)));
        add(new Employee(10005, "Ellen", 3, "SALES", "CHIEF", 300000,
                LocalDate.of(2013, 4, 1)));
        add(new Employee(10006, "Frank", 1, "PLANNING", "ASSOCIATE", 250000,
                LocalDate.of(2013, 10, 1)));
        add(new Employee(10007, "Ivan", 4, "PRODUCT", "MANAGER", 480000,
                LocalDate.of(2014, 1, 1)));
        add(new Employee(10008, "Justin", 2, "HR", "MANAGER", 460000,
                LocalDate.of(2014, 4, 1)));
        add(new Employee(10009, "Mallory", 4, "PRODUCT", "LEADER", 420000,
                LocalDate.of(2014, 7, 1)));
        add(new Employee(10010, "Matilda", 3, "SALES", "ASSOCIATE", 280000,
                LocalDate.of(2015, 8, 1)));
        add(new Employee(10011, "Oscar", 4, "PRODUCT", "CHIEF", 320000,
                LocalDate.of(2015, 11, 1)));
        add(new Employee(10012, "Pat", 4, "PRODUCT", "ASSOCIATE", 240000,
                LocalDate.of(2016, 4, 1)));
        add(new Employee(10013, "Peggy", 3, "SALES", "ASSOCIATE", 270000,
                LocalDate.of(2016, 10, 1)));
        add(new Employee(10014, "Victor", null, null, "ASSOCIATE", 220000,
                LocalDate.of(2017, 4, 1)));
        add(new Employee(10015, "Steve", 1, "PLANNING", "LEADER", 380000,
                LocalDate.of(2017, 10, 1)));
        add(new Employee(10016, "Trent", 4, "PRODUCT", "CHIEF", 310000,
                LocalDate.of(2017, 10, 1)));
    }

    // 初期データの登録
    private void add(Employee employee) {
        employees.put(employee.employeeId(), employee);
    }

    // 全件検索
    public synchronized List<Employee> findAll() {
        return List.copyOf(employees.values());
    }

    // 主キー検索
    public synchronized Employee find(int id) {
        Employee employee = employees.get(id);
        if (employee == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found: " + id);
        }
        return employee;
    }

    // 顧客リソースの新規登録
    public synchronized Employee create(Employee input) {
        Employee employee = withServerFields(nextId, input);
        employees.put(nextId++, employee);
        return employee;
    }

    // アクションメソッド：人物を更新・追加
    public synchronized Employee update(int id, Employee input) {
        find(id);
        Employee employee = withServerFields(id, input);
        employees.put(id, employee);
        return employee;
    }

    // APIメソッド：Employeeの削除
    public synchronized void delete(int id) {
        find(id);
        employees.remove(id);
    }

    // を用いたサーバー項目の実行
    private Employee withServerFields(int id, Employee input) {
        return new Employee(id, input.employeeName(), input.departmentId(),
                departmentName(input.departmentId()), input.jobName(), input.salary(),
                input.entranceDate());
    }

    // 部署名称の実行
    private String departmentName(int id) {
        return switch (id) {
            case 1 -> "PLANNING";
            case 2 -> "HR";
            case 3 -> "SALES";
            case 4 -> "PRODUCT";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown department");
        };
    }
}
