package pro.kensait.jpa.company.main;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

/*
 * JPAbulkdeleteの機能を提供するクラス
 */
public class JpaBulkDeleteMain {
    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // EntityTransactionを開始する
                EntityTransaction entityTransaction = entityManager.getTransaction();
                entityTransaction.begin();

                // バルクで削除する
                Query query = entityManager.createQuery(
                        "DELETE FROM Employee AS e " +
                        "WHERE :salary <= e.salary")
                        .setParameter("salary", 400000);
                Integer hitCount = query.executeUpdate();
                System.out.println("ヒット件数 => " + hitCount); // 更新結果を表示

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
