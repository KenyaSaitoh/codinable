package pro.kensait.jpa.company.main;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

/*
 * JPAbulkupdatemain1の機能を提供するクラス
 */
public class JpaBulkUpdateMain1 {
    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // EntityTransactionを開始する
                EntityTransaction entityTransaction = entityManager.getTransaction();
                entityTransaction.begin();

                // バルクで更新する
                Query query = entityManager.createQuery(
                        "UPDATE Employee AS e " +
                        "SET e.salary = e.salary + :increase " +
                        "WHERE e.salary <= :salary")
                        .setParameter("increase", 2000)
                        .setParameter("salary", 300000);
                Integer updateCount = query.executeUpdate();
                System.out.println("ヒット件数 => " + updateCount); // 更新結果を表示

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
