package pro.kensait.jpa.company.main.lock;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.Persistence;
import pro.kensait.jpa.company.entity.Department;
import pro.kensait.jpa.company.entity.Employee;

/*
 * cascadeoptimisticlockmain1の機能を提供するクラス
 */
public class CascadeOptimisticLockMain1 {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // EntityTransactionを開始する
                EntityTransaction entityTransaction = entityManager.getTransaction();
                entityTransaction.begin();

                Department department = entityManager.find(Department.class, 1);
                List<Employee> employees = department.getEmployees();
                Employee employee = employees.get(0);
                employee.setSalary(520000);

                entityManager.clear();
                entityManager.merge(department);

                // 意図的にスリープする
                try {
                    Thread.sleep(20000);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                try {
                    // 親をマージした場合、カスケードされて子が更新されると、
                    // 子の楽観ロックは有効に作用する
                    entityManager.flush();
                } catch(OptimisticLockException ole) {
                    throw ole;
                    // リカバリ
                }
                entityTransaction.commit();
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
