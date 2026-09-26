package pro.kensait.jpa.company.optimistic.conflictor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import pro.kensait.jpa.company.entity.Employee;

/*
 * lockconflictorの機能を提供するクラス
 */
public class LockConflictor {

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
                Employee employee = entityManager.find(Employee.class, 10003);
                employee.setSalary(employee.getSalary() + 20000);

                // Employeeをマージする
                entityManager.merge(employee);

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
