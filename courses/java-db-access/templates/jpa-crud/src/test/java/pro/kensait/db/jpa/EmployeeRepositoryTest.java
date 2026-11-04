package pro.kensait.db.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/*
 * 社員のテスト
 */
class EmployeeRepositoryTest {
    private static EntityManagerFactory factory;

    // テスト全体の前処理
    @BeforeAll
    static void createFactory() {
        factory = Persistence.createEntityManagerFactory("jpa-crud");
    }

    // テスト全体の後処理
    @AfterAll
    static void closeFactory() {
        factory.close();
    }

    // 「エンティティの更新永続化と削除」の検証
    @Test
    void persistsUpdatesAndRemovesEntity() {
        EntityManager entityManager = factory.createEntityManager();
        EmployeeRepository repository = new EmployeeRepository();

        entityManager.getTransaction().begin();
        repository.add(entityManager,
                new Employee(10017, 3, "Walter", new BigDecimal("230000")));
        entityManager.getTransaction().commit();
        entityManager.clear();

        entityManager.getTransaction().begin();
        Employee employee = repository.find(entityManager, 10017).orElseThrow();
        employee.changeSalary(new BigDecimal("250000"));
        entityManager.getTransaction().commit();
        entityManager.clear();

        Employee changed = repository.find(entityManager, 10017).orElseThrow();
        assertEquals(new BigDecimal("250000.00"), changed.getSalary());
        assertEquals(1, changed.getVersion());

        entityManager.getTransaction().begin();
        repository.remove(entityManager, changed);
        entityManager.getTransaction().commit();
        assertTrue(repository.find(entityManager, 10017).isEmpty());
        entityManager.close();
    }

    // 「callInTransaction による登録と検索」の検証
    // JPA 3.2 の EntityManagerFactory.callInTransaction は、EntityManager の生成・begin・commit
    // （例外時は rollback）・close をまとめて行う。上のテストの begin〜commit の定型を書かずに済む
    @Test
    void persistsAndFindsEntityWithCallInTransaction() {
        EmployeeRepository repository = new EmployeeRepository();

        // 登録（トランザクションは callInTransaction が開始・コミットする）
        Integer id = factory.callInTransaction(entityManager -> {
            Employee employee = new Employee(10018, 3, "Xavier", new BigDecimal("240000"));
            repository.add(entityManager, employee);
            return employee.getId();
        });
        assertEquals(10018, id);

        // 別のトランザクション（別の EntityManager）で検索する
        Employee found = factory.callInTransaction(
                entityManager -> repository.find(entityManager, id).orElseThrow());
        assertEquals("Xavier", found.getName());
        assertEquals(new BigDecimal("240000.00"), found.getSalary());

        // 後始末（戻り値が要らないときは runInTransaction）
        factory.runInTransaction(entityManager -> repository.find(entityManager, id)
                .ifPresent(employee -> repository.remove(entityManager, employee)));
        assertTrue(factory.callInTransaction(
                entityManager -> repository.find(entityManager, id)).isEmpty());
    }
}
