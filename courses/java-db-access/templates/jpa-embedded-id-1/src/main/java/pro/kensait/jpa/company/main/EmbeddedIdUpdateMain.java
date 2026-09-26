package pro.kensait.jpa.company.main;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import pro.kensait.jpa.company.entity.Employee;
import pro.kensait.jpa.company.entity.EmployeePK;

/*
 * embeddedIDupdateの機能を提供するクラス
 */
public class EmbeddedIdUpdateMain {

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
                EmployeePK pk = new EmployeePK("TECH", 10003);

                // 更新対象のEmployeeを取得する
                Employee employee = entityManager.find(Employee.class, pk);

                // Employeeの永続フィールドの値を書き換える
                employee.setSalary(employee.getSalary() + 10000);

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
