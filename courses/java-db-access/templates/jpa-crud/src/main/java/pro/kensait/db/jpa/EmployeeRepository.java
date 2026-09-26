package pro.kensait.db.jpa;

import jakarta.persistence.EntityManager;
import java.util.Optional;

/*
 * JDBC Templateによる社員アクセス
 */
public final class EmployeeRepository {
    // サービスメソッド：足し算の実行
    public void add(EntityManager entityManager, Employee employee) {
        entityManager.persist(employee);
    }

    // 社員の検索
    public Optional<Employee> find(EntityManager entityManager, int id) {
        return Optional.ofNullable(entityManager.find(Employee.class, id));
    }

    // 社員の削除
    public void remove(EntityManager entityManager, Employee employee) {
        entityManager.remove(employee);
    }
}
