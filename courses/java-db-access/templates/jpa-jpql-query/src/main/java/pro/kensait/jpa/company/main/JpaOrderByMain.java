package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import pro.kensait.jpa.company.entity.Employee;

// ソート（ORDER BY）のテスト
@SuppressWarnings("unchecked")
public class JpaOrderByMain {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // ソート（ORDER BY） 1
                {
                System.out.println("##### TEST1 START #####");
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee AS e " +
                        "ORDER BY e.department.departmentId");
                List<Employee> resultList = query.getResultList();
                showEntityList(resultList);
                System.out.println("##### TEST1 END #####\n"); // 検索結果を表示
                }

                // ソート（ORDER BY） 2
                {
                System.out.println("##### TEST2 START #####");
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee AS e " +
                        "ORDER BY e.department.departmentId, e.salary");
                List<Employee> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST2 END #####\n");
                }

                // ソート（ORDER BY） 3
                {
                System.out.println("##### TEST3 START #####");
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee AS e " +
                        "ORDER BY e.salary DESC")
                        .setFirstResult(4)
                        .setMaxResults(3);
                List<Employee> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST3 END #####\n");
                }
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
