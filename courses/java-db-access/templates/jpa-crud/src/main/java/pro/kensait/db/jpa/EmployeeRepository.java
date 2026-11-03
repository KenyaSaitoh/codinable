package pro.kensait.db.jpa;

import jakarta.persistence.EntityManager;
import java.util.Optional;

/*
 * EntityManager を引数で受け取る社員のリポジトリ（begin・commit は呼び出し元が行う）
 */
public final class EmployeeRepository {
    // 社員の登録
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
