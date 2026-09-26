package pro.kensait.jpa.company.main;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import pro.kensait.jpa.company.entity.Employee;

/*
 * JPAbulkupdatemain2の機能を提供するクラス
 */
public class JpaBulkUpdateMain2 {
    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // EntityTransactionを開始する
                EntityTransaction entityTransaction = entityManager.getTransaction();
                entityTransaction.begin();

                Employee employee = entityManager.find(Employee.class, 10001);

                // バルクで更新する
                Query query = entityManager.createQuery(
                        "UPDATE Employee AS e " +
                        "SET e.salary = e.salary + :increase")
                        .setParameter("increase", 2000);
                query.executeUpdate();

                // フラッシュ（SQL発行）する
                entityManager.flush();

                // Employeeの永続フィールドの値を書き換える
                employee.setSalary(employee.getSalary() + 10000);

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
