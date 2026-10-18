package pro.kensait.jakartaee.employee.rest;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/*
 * メモリ内に社員を持つ教材用のデータ（初期データは16件。Victor は部署なし）
 * WAR を配置し直すと初期状態に戻る
 */
@ApplicationScoped
public class EmployeeStore {
    private final Map<Integer, EmployeeTO> employees = new TreeMap<>();

    public EmployeeStore() {
        add(10001, "Alice", 3, "SALES", "MANAGER", 500000, LocalDate.of(2012, 4, 1));
        add(10002, "Bob", 1, "PLANNING", "MANAGER", 450000, LocalDate.of(2012, 4, 1));
        add(10003, "Carol", 2, "HR", "CHIEF", 350000, LocalDate.of(2012, 4, 1));
        add(10004, "Dave", 3, "SALES", "LEADER", 400000, LocalDate.of(2012, 4, 1));
        add(10005, "Ellen", 3, "SALES", "CHIEF", 300000, LocalDate.of(2013, 4, 1));
        add(10006, "Frank", 1, "PLANNING", "ASSOCIATE", 250000, LocalDate.of(2013, 10, 1));
        add(10007, "Ivan", 4, "PRODUCT", "MANAGER", 480000, LocalDate.of(2014, 1, 1));
        add(10008, "Justin", 2, "HR", "MANAGER", 460000, LocalDate.of(2014, 4, 1));
        add(10009, "Mallory", 4, "PRODUCT", "LEADER", 420000, LocalDate.of(2014, 7, 1));
        add(10010, "Matilda", 3, "SALES", "ASSOCIATE", 280000, LocalDate.of(2015, 8, 1));
        add(10011, "Oscar", 4, "PRODUCT", "CHIEF", 320000, LocalDate.of(2015, 11, 1));
        add(10012, "Pat", 4, "PRODUCT", "ASSOCIATE", 240000, LocalDate.of(2016, 4, 1));
        add(10013, "Peggy", 3, "SALES", "ASSOCIATE", 270000, LocalDate.of(2016, 10, 1));
        add(10014, "Victor", null, null, "ASSOCIATE", 220000, LocalDate.of(2017, 4, 1));
        add(10015, "Steve", 1, "PLANNING", "LEADER", 380000, LocalDate.of(2017, 10, 1));
        add(10016, "Trent", 4, "PRODUCT", "CHIEF", 310000, LocalDate.of(2017, 10, 1));
    }

    private void add(int id, String name, Integer departmentId, String departmentName,
            String jobName, int salary, LocalDate entranceDate) {
        employees.put(id, new EmployeeTO(id, name, departmentId, departmentName, jobName, salary, entranceDate));
    }

    public synchronized List<EmployeeTO> findAll() {
        return List.copyOf(employees.values());
    }

    public synchronized Optional<EmployeeTO> find(int id) {
        return Optional.ofNullable(employees.get(id));
    }
}
