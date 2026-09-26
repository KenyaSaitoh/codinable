package pro.kensait.jpa.company.main;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import pro.kensait.jpa.company.entity.EmployeeQueryResult1;
import pro.kensait.jpa.company.entity.EmployeeQueryResult2;

// 名前付きクエリのテスト
public class JpaNamedNativeQueryMain {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // 名前付きクエリで結果クラスを使用
                {
                System.out.println("##### TEST1 START #####");
                Query query = entityManager.createNamedQuery("findEmployeesByDepartmentId1")
                        .setParameter(1, 10006); // EMPLOYEE_IDに10006をセットする
                EmployeeQueryResult1 result =
                        (EmployeeQueryResult1)query.getSingleResult();
                System.out.println(result); // 検索結果を表示
                System.out.println("##### TEST1 END #####\n");
                }

                // 名前付きクエリで結果セットマッピングを使用
                {
                System.out.println("##### TEST2 START #####");
                Query query = entityManager.createNamedQuery("findEmployeesByDepartmentId2")
                        .setParameter(1, 10006); // EMPLOYEE_IDに10006をセットする
                EmployeeQueryResult2 result =
                        (EmployeeQueryResult2)query.getSingleResult();
                System.out.println(result); // 検索結果を表示
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
