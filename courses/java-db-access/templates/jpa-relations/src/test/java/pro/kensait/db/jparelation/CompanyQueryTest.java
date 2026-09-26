package pro.kensait.db.jparelation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/*
 * companyクエリのテスト
 */
class CompanyQueryTest {
    private static EntityManagerFactory factory;

    // 各テストケースで共通的な前処理
    @BeforeAll
    static void setUp() {
        factory = Persistence.createEntityManagerFactory("jpa-relations");
    }

    // テスト全体の後処理
    @AfterAll
    static void tearDown() {
        factory.close();
    }

    // 「関連とCriteria APIによる検索」の検証
    @Test
    void queriesRelationsAndCriteria() {
        EntityManager entityManager = factory.createEntityManager();
        entityManager.getTransaction().begin();
        Department sales = new Department(10, "営業部");
        sales.addEmployee(new Employee(101, "佐藤 花子", new BigDecimal("420000")));
        sales.addEmployee(new Employee(102, "鈴木 一郎", new BigDecimal("380000")));
        entityManager.persist(sales);
        entityManager.getTransaction().commit();
        entityManager.clear();

        CompanyQuery query = new CompanyQuery();
        assertEquals(2, query.findDepartmentsWithEmployees(entityManager)
                .getFirst().getEmployees().size());
        assertEquals(101, query.findEmployeesWithSalaryAtLeast(entityManager,
                new BigDecimal("400000"), 0, 10).getFirst().getId());
        entityManager.close();
    }
}
