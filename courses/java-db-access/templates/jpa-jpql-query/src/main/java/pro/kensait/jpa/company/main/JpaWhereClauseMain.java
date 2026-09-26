package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import pro.kensait.jpa.company.entity.Department;
import pro.kensait.jpa.company.entity.Employee;

// WHERE節の様々な記述方法に関するテスト
@SuppressWarnings("unchecked")
public class JpaWhereClauseMain {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // ARITHMETIC OPERATOR
                {
                System.out.println("##### TEST1 START #####");
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee AS e " +
                        "WHERE e.salary * 0.1 < :salary")
                        .setParameter("salary", 30000);
                List<Employee> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST1 END #####\n");
                }

                // LOGICAL OPERATOR
                {
                System.out.println("##### TEST2 START #####");
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee AS e " +
                        "WHERE :lower <= e.salary AND e.salary <= :upper")
                        .setParameter("lower", 300000)
                        .setParameter("upper", 350000);
                List<Employee> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST2 END #####\n");
                }

                // COMPARISON SYMBOLS
                {
                System.out.println("##### TEST3 START #####");
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee AS e " +
                        "WHERE NOT e.department.departmentId = :departmentId " +
                        "AND ( e.salary <= :lower OR :upper <= e.salary)")
                        .setParameter("departmentId", 2)
                        .setParameter("lower", 300000)
                        .setParameter("upper", 350000);
                List<Employee> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST3 END #####\n");
                }

                // BETWEEN
                {
                System.out.println("##### TEST4 START #####");
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee AS e " +
                        "WHERE e.salary BETWEEN :lower AND :upper")
                        .setParameter("lower", 300000)
                        .setParameter("upper", 350000);
                List<Employee> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST4 END #####\n");
                }

                // NOT BETWEEN
                {
                System.out.println("##### TEST5 START #####");
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee AS e " +
                        "WHERE e.salary NOT BETWEEN :lower AND :upper")
                        .setParameter("lower", 300000)
                        .setParameter("upper", 400000);
                List<Employee> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST5 END #####\n");
                }

                // IN
                {
                System.out.println("##### TEST6 START #####");
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee e " +
                        "WHERE e.department.departmentId IN (1, 5)");
                List<Employee> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST6 END #####\n");
                }

                // NOT IN
                {
                System.out.println("##### TEST7 START #####");
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee e " +
                        "WHERE e.department.departmentId NOT IN (2, 3, 4)");
                List<Employee> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST7 END #####\n");
                }

                // LIKE 1
                {
                System.out.println("##### TEST8 START #####");
                Query query = entityManager.createQuery(
                        "SELECT d FROM Department d " +
                        "WHERE d.location LIKE :location")
                        .setParameter("location", "新宿%");
                List<Department> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST8 END #####\n");
                }

                // LIKE 2
                {
                System.out.println("##### TEST9 START #####");
                Query query = entityManager.createQuery(
                        "SELECT d FROM Department d " +
                        "WHERE d.location LIKE '新宿%'");
                List<Department> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST9 END #####\n");
                }

                // NOT LIKE
                {
                System.out.println("##### TEST10 START #####");
                Query query = entityManager.createQuery(
                        "SELECT d FROM Department d " +
                        "WHERE d.location NOT LIKE :location")
                        .setParameter("location", "新宿%");
                List<Department> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST10 END #####\n");
                }

                // NULL
                {
                System.out.println("##### TEST11 START #####");
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee e " +
                        "WHERE e.department IS NULL");
                List<Employee> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST11 END #####\n");
                }

                // NOT NULL
                {
                System.out.println("##### TEST12 START #####");
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee e " +
                        "WHERE e.department IS NOT NULL");
                List<Employee> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST12 END #####\n");
                }

                // EMPTY
                {
                System.out.println("##### TEST13 START #####");
                Query query = entityManager.createQuery(
                        "SELECT d FROM Department d " +
                        "WHERE d.employees IS EMPTY");
                List<Department> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST13 END #####\n");
                }

                // NOT EMPTY
                {
                System.out.println("##### TEST14 START #####");
                Query query = entityManager.createQuery(
                        "SELECT d FROM Department d " +
                        "WHERE d.employees IS NOT EMPTY");
                List<Department> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST14 END #####\n");
                }
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
