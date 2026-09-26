package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.time.LocalDate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

// 集合関数のテスト
public class JpaAggregateFunctionMain {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // COUNT 1
                {
                System.out.println("##### TEST1 START #####");
                Query query = entityManager.createQuery(
                        "SELECT COUNT(e) FROM Employee e ");
                Long result = (Long)query.getSingleResult();
                showSingleResult(result); // 検索結果を表示
                }

                // COUNT 2
                {
                System.out.println("##### TEST2 START #####");
                Query query = entityManager.createQuery(
                        "SELECT COUNT(DISTINCT e.department.departmentId) " +
                        "FROM Employee e ");
                Long result = (Long)query.getSingleResult();
                showSingleResult(result); // 検索結果を表示
                }

                // AVG
                {
                System.out.println("##### TEST3 START #####");
                Query query = entityManager.createQuery(
                        "SELECT AVG(e.salary) FROM Employee e ");
                Double result = (Double)query.getSingleResult();
                showSingleResult(result); // 検索結果を表示
                }

                // MIN 1
                {
                System.out.println("##### TEST4 START #####");
                Query query = entityManager.createQuery(
                        "SELECT MIN(e.salary) FROM Employee e ");
                Integer result = (Integer)query.getSingleResult();
                showSingleResult(result); // 検索結果を表示
                }

                // MIN 2
                {
                System.out.println("##### TEST5 START #####");
                Query query = entityManager.createQuery(
                        "SELECT MIN(e.entranceDate) FROM Employee e ");
                LocalDate result = (LocalDate)query.getSingleResult();
                showSingleResult(result); // 検索結果を表示
                }

                // MAX
                {
                System.out.println("##### TEST6 START #####");
                Query query = entityManager.createQuery(
                        "SELECT MAX(e.salary) FROM Employee e ");
                Integer result = (Integer)query.getSingleResult();
                showSingleResult(result); // 検索結果を表示
                }

                // SUM
                {
                System.out.println("##### TEST7 START #####");
                Query query = entityManager.createQuery(
                        "SELECT SUM(e.salary) FROM Employee e ");
                Long result = (Long)query.getSingleResult();
                showSingleResult(result); // 検索結果を表示
                }
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
