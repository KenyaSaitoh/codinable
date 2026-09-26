package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import pro.kensait.jpa.company.entity.Department;
import pro.kensait.jpa.company.entity.Employee;

// クエリのテスト
@SuppressWarnings("unchecked")
public class JpaQueryMain {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // 全カラムを指定
                {
                System.out.println("##### TEST1 START #####");
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee AS e " +
                        "WHERE :lower <= e.salary AND e.salary <= :upper")
                        .setParameter("lower", 300000)
                        .setParameter("upper", 400000);
                List<Employee> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST1 END #####\n");
                }

                // カラムを限定
                {
                System.out.println("##### TEST2 START #####");
                Query query = entityManager.createQuery(
                        "SELECT e.employeeId, e.employeeName, e.salary " +
                        "FROM Employee AS e " +
                        "WHERE :lower <= e.salary AND e.salary <= :upper")
                        .setParameter("lower", 300000)
                        .setParameter("upper", 400000);
                List<Object[]> resultList = query.getResultList();
                showColumnsList(resultList); // 検索結果を表示
                System.out.println("##### TEST2 END #####\n");
                }

                // 関連エンティティを条件に指定
                {
                System.out.println("##### TEST3 START #####");
                Department department = entityManager.find(Department.class, 3);
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee AS e " +
                        "WHERE e.department = :department")
                        .setParameter("department", department);
                List<Employee> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST3 END #####\n");
                }

                {
                // 関連エンティティのプロパティを条件に指定
                System.out.println("##### TEST4 START #####");
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee AS e " +
                        "WHERE e.department.departmentId = :departmentId")
                        .setParameter("departmentId", 3);
                List<Employee> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST4 END #####\n");
                }

                // 時間型の永続フィールドを条件に指定
                {
                System.out.println("##### TEST5 START #####");
                LocalDate entranceDate = LocalDate.of(2012, 4, 1);  // 2012年4月1日
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee AS e " +
                        "WHERE e.entranceDate = :entranceDate")
                        .setParameter("entranceDate", entranceDate);
                List<Employee> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST5 END #####\n");
                }
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
