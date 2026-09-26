package pro.kensait.jpa.company.main;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

// ネイティブクエリのテスト
public class JpaNativeQueryMain4 {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // EntityTransactionを開始する
                EntityTransaction entityTransaction = entityManager.getTransaction();
                entityTransaction.begin();

                // NATIVE QUERY
                Query query = entityManager.createNativeQuery(
                        "UPDATE EMPLOYEE " +
                        "SET SALARY = SALARY + ?2 " +
                        "WHERE DEPARTMENT_ID = ?1")
                        .setParameter(2, 2000)
                        .setParameter(1, 2);
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
