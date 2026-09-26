package pro.kensait.jpa.company.optimistic.main;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.Persistence;
import pro.kensait.jpa.company.entity.Employee;

/*
 * lockの機能を提供するクラス
 */
public class LockMain {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // EntityTransactionを開始する
                EntityTransaction entityTransaction = entityManager.getTransaction();
                entityTransaction.begin();

                // 更新対象のEmployeeを取得し、月給を10000加算する
                Employee employee = entityManager.find(Employee.class, 10003);
                employee.setSalary(employee.getSalary() + 10000);

                // Employeeをマージする
                entityManager.merge(employee);

                // 意図的に20秒間スリープする
                try {
                    Thread.sleep(20000);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                try {
                    entityManager.flush();
                } catch(OptimisticLockException ole) {
                    throw ole;
                    // リカバリ
                }

                // EntityTransactionをコミットする
                entityTransaction.commit();
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
