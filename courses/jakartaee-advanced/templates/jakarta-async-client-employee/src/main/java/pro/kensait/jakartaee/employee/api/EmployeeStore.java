package pro.kensait.jakartaee.employee.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/*
 * メモリ内に社員を持つ教材用のデータ（初期データは16件。Victor は部署なし）
 * WAR を配置し直す（▶ アプリ実行し直す）と初期状態に戻る。新しい社員には 10017 から番号を振る
 */
@ApplicationScoped
public class EmployeeStore {
    private final Map<Integer, EmployeeTO> employees = new TreeMap<>();
    private int nextId = 10017;

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

    private void add(int id, String name, Integer departmentId, String jobName, int salary, LocalDate entranceDate) {
        employees.put(id, new EmployeeTO(id, name, departmentId,
                departmentId == null ? null : departmentName(departmentId), jobName, salary, entranceDate));
    }

    public synchronized List<EmployeeTO> findAll() {
        return List.copyOf(employees.values());
    }

    public synchronized EmployeeTO find(int id) {
        EmployeeTO employee = employees.get(id);
        if (employee == null) {
            throw new NotFoundException("Employee not found: " + id);
        }
        return employee;
    }

    public synchronized EmployeeTO create(EmployeeTO input) {
        EmployeeTO employee = withServerFields(nextId, input);
        employees.put(nextId++, employee);
        return employee;
    }

    public synchronized EmployeeTO update(int id, EmployeeTO input) {
        find(id);
        EmployeeTO employee = withServerFields(id, input);
        employees.put(id, employee);
        return employee;
    }

    public synchronized void delete(int id) {
        find(id);
        employees.remove(id);
    }

    // 社員ID と部署名はサーバーが決める（送られてきた値は使わない）
    private EmployeeTO withServerFields(int id, EmployeeTO input) {
        return new EmployeeTO(id, input.employeeName(), input.departmentId(),
                departmentName(input.departmentId()), input.jobName(), input.salary(), input.entranceDate());
    }

    private String departmentName(int id) {
        return switch (id) {
            case 1 -> "PLANNING";
            case 2 -> "HR";
            case 3 -> "SALES";
            case 4 -> "PRODUCT";
            default -> throw new BadRequestException("Unknown department: " + id);
        };
    }
}
