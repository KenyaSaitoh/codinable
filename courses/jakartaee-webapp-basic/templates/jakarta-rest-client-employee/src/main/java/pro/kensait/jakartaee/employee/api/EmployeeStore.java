package pro.kensait.jakartaee.employee.api;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** メモリー内だけで保持する確認用のデータ。デプロイし直すと初期状態（16 件）に戻る */
@ApplicationScoped
public class EmployeeStore {
    private final Map<Integer, Employee> employees = new TreeMap<>();
    private int nextId = 10017;

    // 初期データ（Victor は部署なし）
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

    private void add(int id, String name, Integer departmentId, String jobName, int salary,
            LocalDate entranceDate) {
        employees.put(id, new Employee(id, name, departmentId,
                departmentId == null ? null : departmentName(departmentId), jobName, salary,
                entranceDate));
    }

    public synchronized List<Employee> findAll() {
        return List.copyOf(employees.values());
    }

    // 見つからなければ 404（本文は理由のテキスト）
    public synchronized Employee find(int id) {
        Employee employee = employees.get(id);
        if (employee == null) {
            throw new NotFoundException(Response.status(Response.Status.NOT_FOUND)
                    .type(MediaType.TEXT_PLAIN).entity("Employee not found: " + id).build());
        }
        return employee;
    }

    public synchronized Employee create(Employee input) {
        Employee employee = withServerFields(nextId, input);
        employees.put(nextId++, employee);
        return employee;
    }

    public synchronized Employee update(int id, Employee input) {
        find(id);
        Employee employee = withServerFields(id, input);
        employees.put(id, employee);
        return employee;
    }

    public synchronized void delete(int id) {
        find(id);
        employees.remove(id);
    }

    // 社員ID と部署名はサーバー側で決める
    private Employee withServerFields(int id, Employee input) {
        return new Employee(id, input.employeeName(), input.departmentId(),
                departmentName(input.departmentId()), input.jobName(), input.salary(),
                input.entranceDate());
    }

    private String departmentName(int id) {
        return switch (id) {
            case 1 -> "PLANNING";
            case 2 -> "HR";
            case 3 -> "SALES";
            case 4 -> "PRODUCT";
            default -> throw new BadRequestException("Unknown department");
        };
    }
}
