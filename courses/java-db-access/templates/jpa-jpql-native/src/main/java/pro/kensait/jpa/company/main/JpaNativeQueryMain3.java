package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import pro.kensait.jpa.company.entity.EmployeeQueryResult2;

// ネイティブクエリのテスト
@SuppressWarnings("unchecked")
public class JpaNativeQueryMain3 {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // SQL RESULT SET MAPPING 1
                {
                System.out.println("##### TEST7 START #####");
                // ネイティブクエリを生成する
                Query query = entityManager.createNativeQuery(
                        "SELECT e.EMPLOYEE_ID AS E_EMPLOYEE_ID, " +
                        "e.EMPLOYEE_NAME AS E_EMPLOYEE_NAME, " +
                        "d.DEPARTMENT_NAME AS D_DEPARTMENT_NAME, " +
                        "e.SALARY AS E_SALARY, " +
                        "d.LOCATION AS D_LOCATION " +
                        "FROM EMPLOYEE e, DEPARTMENT d " +
                        "WHERE e.DEPARTMENT_ID = d.DEPARTMENT_ID " +
                        "AND e.EMPLOYEE_ID = ?1",
                        "NativeQueryResult1")
                        .setParameter(1, 10006); // EMPLOYEE_IDに10006をセットする
                List<EmployeeQueryResult2> resultList =
                        (List<EmployeeQueryResult2>)query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST7 END #####\n");
                }

                // SQL RESULT SET MAPPING 2
                {
                System.out.println("##### TEST8 START #####");
                // ネイティブクエリを生成する
                Query query = entityManager.createNativeQuery(
                        "SELECT e.SALARY AS E_SALARY, " +
                        "d.LOCATION AS D_LOCATION " +
                        "FROM EMPLOYEE e, DEPARTMENT d " +
                        "WHERE e.DEPARTMENT_ID = d.DEPARTMENT_ID " +
                        "AND e.EMPLOYEE_ID = ?1",
                        "NativeQueryResult2")
                        .setParameter(1, 10006);  // EMPLOYEE_IDに10006をセットする
                List<Object[]> resultList = (List<Object[]>)query.getResultList();
                showColumnsList(resultList); // 検索結果を表示
                System.out.println("##### TEST8 END #####\n");
                }

                // SQL RESULT SET MAPPING 3
                {
                System.out.println("##### TEST9 START #####");
                // ネイティブクエリを生成する
                Query query = entityManager.createNativeQuery(
                        "SELECT e.EMPLOYEE_ID AS E_EMPLOYEE_ID, " +
                        "e.EMPLOYEE_NAME AS E_EMPLOYEE_NAME, " +
                        "d.DEPARTMENT_NAME AS D_DEPARTMENT_NAME, " +
                        "e.SALARY AS E_SALARY, " +
                        "d.LOCATION AS D_LOCATION " +
                        "FROM EMPLOYEE e, DEPARTMENT d " +
                        "WHERE e.DEPARTMENT_ID = d.DEPARTMENT_ID " +
                        "AND e.EMPLOYEE_ID = ?1",
                        "NativeQueryResult3")
                        .setParameter(1, 10006);  // EMPLOYEE_IDに10006をセットする
                List<Object[]> resultList = (List<Object[]>)query.getResultList();
                showColumnsList(resultList); // 検索結果を表示
                System.out.println("##### TEST9 END #####\n");
                }
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
