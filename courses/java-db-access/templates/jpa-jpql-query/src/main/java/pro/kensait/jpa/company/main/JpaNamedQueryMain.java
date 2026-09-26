package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import pro.kensait.jpa.company.entity.Employee;

// 名前付きクエリのテスト
@SuppressWarnings("unchecked")
public class JpaNamedQueryMain {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // 名前付きクエリ
                Query query = entityManager.createNamedQuery("findEmployeesBysalary")
                        .setParameter("salary", 400000);
                List<Employee> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
