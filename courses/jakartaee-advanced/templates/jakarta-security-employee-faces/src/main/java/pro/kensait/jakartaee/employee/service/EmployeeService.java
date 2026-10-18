package pro.kensait.jakartaee.employee.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/*
 * 社員の業務処理（メモリ内に社員を持つ。初期データは Alice から Trent までの16件）
 * WAR を配置し直すと初期状態に戻る。新しい社員には 10017 から番号を振る
 */
@ApplicationScoped
public class EmployeeService {
    private final Map<Integer, Employee> employees = new TreeMap<>();
    private int nextId = 10017;

    public EmployeeService() {
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

    private void add(int id, String name, Integer departmentId, String jobName, int salary, LocalDate entranceDate) {
        employees.put(id, new Employee(id, name, departmentId,
                departmentId == null ? null : departmentName(departmentId), jobName, salary, entranceDate));
    }

    public synchronized List<Employee> findAll() {
        return List.copyOf(employees.values());
    }

    public synchronized Employee find(int id) {
        Employee employee = employees.get(id);
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found: " + id);
        }
        return employee;
    }

    // id が null なら登録、そうでなければ更新
    public synchronized Employee save(Integer id, Employee input) {
        int key = id == null ? nextId++ : find(id).employeeId();
        Employee employee = new Employee(key, input.employeeName(), input.departmentId(),
                departmentName(input.departmentId()), input.jobName(), input.salary(), input.entranceDate());
        employees.put(key, employee);
        return employee;
    }

    public synchronized void delete(int id) {
        find(id);
        employees.remove(id);
    }

    private String departmentName(int id) {
        return switch (id) {
            case 1 -> "PLANNING";
            case 2 -> "HR";
            case 3 -> "SALES";
            case 4 -> "PRODUCT";
            default -> throw new IllegalArgumentException("Unknown department: " + id);
        };
    }
}
