package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import pro.kensait.jpa.company.entity.Department;
import pro.kensait.jpa.company.entity.Employee;

/*
 * embeddableselectの機能を提供するクラス
 */
public class EmbeddableSelectMain {

    public static void main(String[] args) {
        {
            System.out.println("***** snippet_1 *****");
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
        {
            System.out.println("***** snippet_2 *****");
            // EntityManagerFactory → EntityManagerを取得する
            try (EntityManagerFactory entityManagerFactory =
                    Persistence.createEntityManagerFactory("MyPersistenceUnit");
                    EntityManager entityManager = entityManagerFactory.createEntityManager()) {
                try {

                    // Departmentを取得する
                    Department department = entityManager.find(Department.class, 4);

                    // 検索結果をコンソールに表示する
                    showEntity(department);
                } finally {
                    if (entityManager.getTransaction().isActive()) {
                        entityManager.getTransaction().rollback();
                    }
                }
            }
        }
    }
}
