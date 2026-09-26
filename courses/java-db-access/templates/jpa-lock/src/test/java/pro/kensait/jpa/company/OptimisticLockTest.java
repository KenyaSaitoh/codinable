package pro.kensait.jpa.company;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;
import pro.kensait.course.SampleDatabase;
import pro.kensait.jpa.company.entity.Employee;

/*
 * optimisticlockのテスト
 */
class OptimisticLockTest {
    // 「古いバージョンによる更新の拒否」の検証
    @Test
    void rejectsAnUpdateBasedOnAnOldVersion() throws Exception {
        SampleDatabase.reset();
        try (var factory = Persistence.createEntityManagerFactory("MyPersistenceUnit");
                var first = factory.createEntityManager();
                var second = factory.createEntityManager()) {
            var winner = first.find(Employee.class, 10003);
            var stale = second.find(Employee.class, 10003);
            try {
                first.getTransaction().begin();
                winner.setSalary(360000);
                first.getTransaction().commit();

                second.getTransaction().begin();
                stale.setSalary(999999);
                assertThrows(OptimisticLockException.class, second::flush);
            } finally {
                if (first.getTransaction().isActive()) {
                    first.getTransaction().rollback();
                }
                if (second.getTransaction().isActive()) {
                    second.getTransaction().rollback();
                }
            }
            first.clear();
            assertEquals(360000, first.find(Employee.class, 10003).getSalary());
        }
    }
}
