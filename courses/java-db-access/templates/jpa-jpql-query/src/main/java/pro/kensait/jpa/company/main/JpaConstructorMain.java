package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import pro.kensait.jpa.company.dto.EmpDept;
import pro.kensait.jpa.company.dto.EmployeeTO;

// コンストラクタ式のテスト
public class JpaConstructorMain {

    public static void main(String[] args) {
        // EntityManagerFactoryを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                {
                System.out.println("##### TEST1 START #####");
                Query query = entityManager.createQuery(
                        "SELECT NEW pro.kensait.jpa.company.dto.EmployeeTO" +
                        "(e.employeeId, e.employeeName, d.departmentName) " +
                        "FROM Department AS d INNER JOIN d.employees AS e " +
                        "WHERE e.employeeId = :employeeId")
                        .setParameter("employeeId", 10008);
                EmployeeTO employeeTO = (EmployeeTO)query.getSingleResult();
                showEntity(employeeTO); // 検索結果を表示
                System.out.println("##### TEST1 END #####\n");
                }

                // コンストラクタ式 2
                {
                System.out.println("##### TEST2 START #####");
                Query query = entityManager.createQuery(
                        "SELECT NEW pro.kensait.jpa.company.dto.EmpDept" +
                        "(e.employeeId, e.employeeName, e.salary, d.departmentId, " +
                        "d.departmentName, d.location) " +
                        "FROM Employee AS e INNER JOIN e.department AS d " +
                        "WHERE e.employeeId = :employeeId")
                        .setParameter("employeeId", 10001);
                EmpDept empDept = (EmpDept)query.getSingleResult();
                showEntity(empDept); // 検索結果を表示
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
