package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

// パス式のテスト
public class JpaPathExpressionMain {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // パス式
                {
                Query query = entityManager.createQuery(
                        "SELECT e.employeeId, e.employeeName, " +
                        "e.department.departmentName " +
                        "FROM Employee AS e " +
                        "WHERE e.employeeId = :employeeId")
                        .setParameter("employeeId", 10006);
                Object[] objs = (Object[])query.getSingleResult();
                showColumns(objs); // 検索結果を表示
                }
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
