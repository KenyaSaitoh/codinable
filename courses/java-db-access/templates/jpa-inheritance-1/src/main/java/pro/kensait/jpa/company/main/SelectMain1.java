package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import pro.kensait.jpa.company.entity.Employee;

// 検索（主キーから）
public class SelectMain1 {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // Employeeを取得する
                Employee employee = entityManager.find(Employee.class, 10001);

                // 検索結果をコンソールに表示する
                showEntity(employee);
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
