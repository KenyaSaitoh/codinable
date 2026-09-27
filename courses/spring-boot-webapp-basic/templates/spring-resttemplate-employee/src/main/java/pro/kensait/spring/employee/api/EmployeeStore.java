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

    // 社員storeの初期化（全講座共通の16件。Victor は部署なし）
    public EmployeeStore() {
        add(10001, "Alice", 3, "MANAGER", 500000, LocalDate.of(2012, 4, 1));
        add(10002, "Bob", 1, "MANAGER", 450000, LocalDate.of(2012, 4, 1));
        add(10003, "Carol", 2, "CHIEF", 350000, LocalDate.of(2012, 4, 1));
        add(10004, "Dave", 3, "LEADER", 400000, LocalDate.of(2012, 4, 1));
        add(10005, "Ellen", 3, "CHIEF", 300000, LocalDate.of(2013, 4, 1));
        add(10006, "Frank", 1, "ASSOCIATE", 250000, LocalDate.of(2013, 10, 1));
        add(10007, "Ivan", 4, "MANAGER", 480000, LocalDate.of(2014, 1, 1));
        add(10008, "Justin", 2, "MANAGER", 460000, LocalDate.of(2014, 4, 1));
        add(10009, "Mallory", 4, "LEADER", 420000, LocalDate.of(2014, 7, 1));
        add(10010, "Matilda", 3, "ASSOCIATE", 280000, LocalDate.of(2015, 8, 1));
        add(10011, "Oscar", 4, "CHIEF", 320000, LocalDate.of(2015, 11, 1));
        add(10012, "Pat", 4, "ASSOCIATE", 240000, LocalDate.of(2016, 4, 1));
        add(10013, "Peggy", 3, "ASSOCIATE", 270000, LocalDate.of(2016, 10, 1));
        add(10014, "Victor", null, "ASSOCIATE", 220000, LocalDate.of(2017, 4, 1));
        add(10015, "Steve", 1, "LEADER", 380000, LocalDate.of(2017, 10, 1));
        add(10016, "Trent", 4, "CHIEF", 310000, LocalDate.of(2017, 10, 1));
    }

    // 初期社員の追加
    private void add(int id, String name, Integer departmentId, String jobName, int salary,
            LocalDate entranceDate) {
        employees.put(id, new Employee(id, name, departmentId,
                departmentId == null ? null : departmentName(departmentId), jobName, salary,
                entranceDate));
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
