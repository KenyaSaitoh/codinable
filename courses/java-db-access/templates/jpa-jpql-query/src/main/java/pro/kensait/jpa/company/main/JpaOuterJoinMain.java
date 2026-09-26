package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

//外部結合（アウタージョイン）のテスト
@SuppressWarnings("unchecked")
public class JpaOuterJoinMain {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // 外部結合（アウタージョイン）1
                {
                System.out.println("##### TEST1 START #####");
                Query query = entityManager.createQuery(
                        "SELECT e.employeeId, e.employeeName, d.departmentName " +
                        "FROM Employee AS e LEFT OUTER JOIN e.department AS d");
                List<Object[]> resultList = query.getResultList();
                showColumnsList(resultList); // 検索結果を表示
                System.out.println("##### TEST1 END #####\n");
                }

                // 外部結合（アウタージョイン）2
                {
                System.out.println("##### TEST2 START #####");
                Query query = entityManager.createQuery(
                        "SELECT e.employeeId, e.employeeName, d.departmentName " +
                        "FROM Department AS d LEFT OUTER JOIN d.employees AS e");
                List<Object[]> resultList = query.getResultList();
                showColumnsList(resultList); // 検索結果を表示
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
