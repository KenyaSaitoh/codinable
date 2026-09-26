package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import pro.kensait.jpa.company.entity.Employee;
import pro.kensait.jpa.company.entity.EmployeePK;

// 検索（主キーから）
public class EmbeddedIdSelectMain {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // 主キーを表すEmployeePKを生成する
                EmployeePK pk = new EmployeePK("TECH", 10001);

                // Employeeを取得する
                Employee employee = entityManager.find(Employee.class, pk);

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
