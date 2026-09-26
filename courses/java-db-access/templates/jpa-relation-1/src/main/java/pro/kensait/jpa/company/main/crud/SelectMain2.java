package pro.kensait.jpa.company.main.crud;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import pro.kensait.jpa.company.entity.Employee;

//検索（主キー以外の条件から）
public class SelectMain2 {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // Queryを生成し、実行する
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee e " +
                        "WHERE :lower <= e.salary AND e.salary <= :upper")
                        .setParameter("lower", 300000)
                        .setParameter("upper", 450000);
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
