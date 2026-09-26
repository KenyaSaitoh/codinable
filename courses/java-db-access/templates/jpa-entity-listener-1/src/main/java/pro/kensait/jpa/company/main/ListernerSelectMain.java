package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import pro.kensait.jpa.company.entity.Employee;

/*
 * listernerselectの機能を提供するクラス
 */
public class ListernerSelectMain {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // Employeeを取得する
                Employee employee = entityManager.find(Employee.class, 10001);

                // Employeeのゲッタにより、カラム値を取得する
                String employeeName = employee.getEmployeeName();

                // 検索結果をコンソールに表示する
                showSingleResult("employeeName", employeeName);
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
