package pro.kensait.jpa.company.main.crud;

import java.time.LocalDate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import pro.kensait.jpa.company.entity.Department;
import pro.kensait.jpa.company.entity.Employee;
import pro.kensait.jpa.company.type.JobType;

// 挿入
public class InsertMain {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // EntityTransactionを開始する
                EntityTransaction entityTransaction = entityManager.getTransaction();
                entityTransaction.begin();

                // Department（配属予定の部署）を取得する
                Department department = entityManager.find(Department.class, 3);

                // 新しいEmployeeを生成する
                Integer employeeId = 10021;
                Employee employee = new Employee(employeeId, "Steve", department,
                        LocalDate.of(2017, 10, 1), JobType.LEADER, 380000, null);

                // Employeeを保存する
                entityManager.persist(employee);

                // EntityTransactionをコミットする
                entityTransaction.commit();
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
