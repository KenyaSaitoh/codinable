package pro.kensait.jpa.company.main;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

// Distinct演算子のテスト
@SuppressWarnings("unchecked")
public class JpaDistinctMain {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // DISTINCTありのケース
                {
                System.out.println("##### TEST1 START #####");
                Query query = entityManager.createQuery(
                        "SELECT DISTINCT d.location FROM Department AS d");
                List<String> resultList = query.getResultList();
                System.out.println(resultList); // 検索結果を表示
                System.out.println("##### TEST1 END #####\n");
                }

                // DISTINCTなしのケース
                {
                System.out.println("##### TEST2 START #####");
                Query query = entityManager.createQuery(
                        "SELECT d.location FROM Department AS d");
                List<String> resultList = query.getResultList();
                System.out.println(resultList); // 検索結果を表示
                System.out.println("##### TEST2 END #####\n");
                }
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
