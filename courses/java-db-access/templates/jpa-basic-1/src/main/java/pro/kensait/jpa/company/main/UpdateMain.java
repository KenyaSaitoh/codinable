package pro.kensait.jpa.company.main;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import pro.kensait.jpa.company.entity.Employee;

/*
 * UPDATE（社員の属性変更）
 */
public class UpdateMain {

    public static void main(String[] args) {
        // EntityManagerFactoryを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // EntityTransactionを開始する
                EntityTransaction entityTransaction = entityManager.getTransaction();
                entityTransaction.begin();

                // 更新対象のEmployeeを取得する
                Employee employee = entityManager.find(Employee.class, 10001);

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
