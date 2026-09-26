package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import pro.kensait.jpa.company.entity.Department;
import pro.kensait.jpa.company.entity.Employee;

// 機能関数のテスト
@SuppressWarnings("unchecked")
public class JpaGeneralFunctionMain {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // LENGTH
                {
                System.out.println("##### TEST1 START #####");
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee AS e " +
                        "WHERE 5 < LENGTH(e.employeeName)");
                List<Employee> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST1 END #####\n");
                }

                // SIZE
                {
                System.out.println("##### TEST2 START #####");
                Query query = entityManager.createQuery(
                        "SELECT d FROM Department AS d " +
                        "WHERE SIZE(d.employees) = 5");
                List<Department> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST2 END #####\n");
                }

                // SUBSTRING
                {
                System.out.println("##### TEST3 START #####");
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee AS e " +
                        "WHERE SUBSTRING(e.employeeName, 2, 1) = 'a'");
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
