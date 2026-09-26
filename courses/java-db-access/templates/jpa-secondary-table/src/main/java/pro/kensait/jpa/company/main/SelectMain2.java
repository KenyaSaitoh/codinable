package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import pro.kensait.jpa.company.entity.Employee;

// クエリのテスト
public class SelectMain2 {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee AS e " +
                        "WHERE :lower <= e.salary AND e.salary <= :upper")
                        .setParameter("lower", 300000)
                        .setParameter("upper", 400000);
                List<Employee> resultList = query.getResultList();

                // 検索結果をコンソールに表示する
                showEntityList(resultList);
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
