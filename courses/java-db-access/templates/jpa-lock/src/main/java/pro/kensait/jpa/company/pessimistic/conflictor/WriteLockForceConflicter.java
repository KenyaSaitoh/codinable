package pro.kensait.jpa.company.pessimistic.conflictor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Persistence;
import pro.kensait.jpa.company.entity.Employee;
/*
 * writelockforceconflicterの機能を提供するクラス
 */
public class WriteLockForceConflicter {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // EntityTransactionを開始する
                EntityTransaction entityTransaction = entityManager.getTransaction();
                entityTransaction.begin();

                // 更新対象のEmployeeを取得し、月給を20000加算する
                Employee employee = entityManager.find(Employee.class, 10003,
                        LockModeType.PESSIMISTIC_FORCE_INCREMENT);
                employee.setSalary(employee.getSalary() + 20000);

                // フラッシュしてSQLを発行する
                entityManager.flush();

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
