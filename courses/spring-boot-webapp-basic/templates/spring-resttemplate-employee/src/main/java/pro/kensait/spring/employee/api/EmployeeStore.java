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
    private int nextId = 7;

    // 社員storeの初期化
    public EmployeeStore() {
        for (int id = 1; id <= 6; id++) {
            int departmentId = ((id - 1) % 3 + 1) * 10;
            employees.put(id, new Employee(id, "Employee " + id, departmentId,
                    departmentName(departmentId), "Staff", 250000 + id * 50000,
                    LocalDate.of(2021, 4, 1)));
        }
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
            case 10 -> "Sales";
            case 20 -> "Engineering";
            case 30 -> "Administration";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown department");
        };
    }
}
