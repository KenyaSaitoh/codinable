package pro.kensait.jpa.company.main;

import java.time.LocalDate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import pro.kensait.jpa.company.entity.Employee;
import pro.kensait.jpa.company.type.JobType;

/*
 * 挿入
 */
public class InsertMain {

    public static void main(String[] args) {
        // EntityManagerFactoryを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // EntityTransactionを開始する
                EntityTransaction entityTransaction = entityManager.getTransaction();
                entityTransaction.begin();

                // 保存対象のEmployeeクラスのインスタンスを生成する
                LocalDate entranceDate = LocalDate.of(2024, 4, 1);
                Employee employee = new Employee(10021, "Steve", "SALES",
                        entranceDate, JobType.LEADER, 380000);
                // 生成したEmployeeを保存する
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
