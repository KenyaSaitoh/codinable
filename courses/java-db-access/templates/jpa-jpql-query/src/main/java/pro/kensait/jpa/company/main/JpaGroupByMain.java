package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import pro.kensait.jpa.company.dto.EmployeeCountTO;

// グルーピングのテスト
@SuppressWarnings("unchecked")
public class JpaGroupByMain {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // グルーピング 1
                {
                System.out.println("##### TEST 1 #####");
                Query query = entityManager.createQuery(
                        "SELECT e.department, COUNT(e) " +
                        "FROM Employee AS e " +
                        "GROUP BY e.department");
                List<Object[]> resultList = query.getResultList();
                showColumnsList(resultList); // 検索結果を表示
                }

                //  グルーピング 2
                {
                System.out.println("##### TEST 2 #####");
                Query query = entityManager.createQuery(
                        "SELECT e.department.departmentId, COUNT(e) " +
                        "FROM Employee AS e " +
                        "GROUP BY e.department.departmentId");
                List<Object[]> resultList = query.getResultList();
                showColumnsList(resultList); // 検索結果を表示
                }

                // グルーピング 3
                {
                System.out.println("##### TEST 3 #####");
                Query query = entityManager.createQuery(
                        "SELECT NEW pro.kensait.jpa.company.dto.EmployeeCountTO" +
                        "(e.department.departmentId, COUNT(e)) " +
                        "FROM Employee AS e " +
                        "GROUP BY e.department.departmentId " +
                        "HAVING COUNT(e.department.departmentId) <= 3");
                List<EmployeeCountTO> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                }

                // グルーピング 4
                {
                System.out.println("##### TEST 4 #####");
                Query query = entityManager.createQuery(
                        "SELECT e.department.departmentId, COUNT(e) " +
                        "FROM Employee AS e " +
                        "GROUP BY e.department.departmentId " +
                        "HAVING COUNT(e.department.departmentId) <= 3");
                List<Object[]> resultList = query.getResultList();
                showColumnsList(resultList); // 検索結果を表示
                }
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
