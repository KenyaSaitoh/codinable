package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import pro.kensait.jpa.company.entity.Department;
import pro.kensait.jpa.company.entity.Employee;

// フラッシュモードのテスト
@SuppressWarnings("unchecked")
public class JpaFlashModeMain {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // EntityTransactionを開始する
                EntityTransaction entityTransaction = entityManager.getTransaction();
                entityTransaction.begin();

                // FlushModeType.AUTO
                {
                System.out.println("##### TEST1 START #####");
                entityManager.setFlushMode(FlushModeType.AUTO);
                Employee employee = entityManager.find(Employee.class, 10013);
                Department department = entityManager.find(Department.class, 1);
                employee.setDepartment(department);
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee AS e " +
                        "WHERE e.department.departmentId = 1");
                List<Employee> resultList = query.getResultList();
                // EntityTransactionをコミットする
                entityTransaction.commit();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST1 END #####\n");
                }

                // FlushModeType.COMMIT
                {
                entityManager.clear();
                entityTransaction.begin();
                System.out.println("##### TEST2 START #####");
                entityManager.setFlushMode(FlushModeType.COMMIT);
                Employee employee = entityManager.find(Employee.class, 10014);
                Department department = entityManager.find(Department.class, 2);
                employee.setDepartment(department);
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee AS e " +
                        "WHERE e.department.departmentId = 2");
                List<Employee> resultList = query.getResultList();
                // EntityTransactionをコミットする
                entityTransaction.commit();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST2 END #####\n");
                }
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
