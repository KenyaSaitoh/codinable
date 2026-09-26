package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

// ネイティブクエリのテスト
@SuppressWarnings("unchecked")
public class JpaNativeQueryMain1 {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // NATIVE QUERY 1
                {
                System.out.println("##### TEST1 START #####");
                Query query = entityManager.createNativeQuery(
                        "SELECT EMPLOYEE_NAME FROM EMPLOYEE " +
                        "WHERE EMPLOYEE_ID = ?1")
                        .setParameter(1, 10001);
                String result = (String)query.getSingleResult();
                showEntity(result); // 検索結果を表示
                System.out.println("##### TEST1 END #####\n");
                }

                // NATIVE QUERY 2
                {
                System.out.println("##### TEST2 START #####");
                Query query = entityManager.createNativeQuery(
                        "SELECT EMPLOYEE_NAME FROM EMPLOYEE " +
                        "WHERE DEPARTMENT_ID = ?1")
                        .setParameter(1, 2);
                List<Object> resultList = (List<Object>)query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST2 END #####\n");
                }

                // NATIVE QUERY 3
                {
                System.out.println("##### TEST3 START #####");
                Query query = entityManager.createNativeQuery(
                        "SELECT EMPLOYEE_NAME, SALARY FROM EMPLOYEE " +
                        "WHERE EMPLOYEE_ID = ?1")
                        .setParameter(1, 10001);
                Object[] result = (Object[])query.getSingleResult();
                showColumns(result); // 検索結果を表示
                System.out.println("##### TEST3 END #####\n");
                }

                // NATIVE QUERY 4
                {
                System.out.println("##### TEST4 START #####");
                Query query = entityManager.createNativeQuery(
                        "SELECT EMPLOYEE_NAME, SALARY FROM EMPLOYEE " +
                        "WHERE DEPARTMENT_ID = ?1")
                        .setParameter(1, 2);
                List<Object[]> resultList = (List<Object[]>)query.getResultList();
                showColumnsList(resultList); // 検索結果を表示
                System.out.println("##### TEST4 END #####\n");
                }
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
