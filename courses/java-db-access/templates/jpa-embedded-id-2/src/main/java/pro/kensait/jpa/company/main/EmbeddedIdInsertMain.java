package pro.kensait.jpa.company.main;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import pro.kensait.jpa.company.entity.Employee;
import pro.kensait.jpa.company.entity.Subsidiary;

/*
 * embeddedIDinsertの機能を提供するクラス
 */
public class EmbeddedIdInsertMain {

    public static void main(String[] args) {
        // EntityManagerFactoryを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // EntityTransactionを開始する
                EntityTransaction entityTransaction = entityManager.getTransaction();
                entityTransaction.begin();

                // 関連するSubsidiaryを取得する
                Subsidiary subsidiary = entityManager.find(Subsidiary.class, 3);

                // 保存対象のEmployeeインスタンスを生成する
                Employee employee = new Employee(10021, "Steve", subsidiary, 380000);

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
