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
                new Employee(101, 10, "佐藤 花子", new BigDecimal("420000")));
        entityManager.getTransaction().commit();
        entityManager.clear();

        entityManager.getTransaction().begin();
        Employee employee = repository.find(entityManager, 101).orElseThrow();
        employee.changeSalary(new BigDecimal("450000"));
        entityManager.getTransaction().commit();
        entityManager.clear();

        Employee changed = repository.find(entityManager, 101).orElseThrow();
        assertEquals(new BigDecimal("450000.00"), changed.getSalary());
        assertEquals(1, changed.getVersion());

        entityManager.getTransaction().begin();
        repository.remove(entityManager, changed);
        entityManager.getTransaction().commit();
        assertTrue(repository.find(entityManager, 101).isEmpty());
        entityManager.close();
    }
}
