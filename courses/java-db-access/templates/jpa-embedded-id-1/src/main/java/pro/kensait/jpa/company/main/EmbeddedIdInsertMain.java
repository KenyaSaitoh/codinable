package pro.kensait.jpa.company.main;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import pro.kensait.jpa.company.entity.Employee;
import pro.kensait.jpa.company.entity.EmployeePK;

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

                // 主キーを表すEmployeePKを生成する
                EmployeePK pk = new EmployeePK("TECH", 10021);

                // 保存対象のEmployeeインスタンスを生成する
                Employee employee = new Employee(pk, "Steve", 380000);

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
