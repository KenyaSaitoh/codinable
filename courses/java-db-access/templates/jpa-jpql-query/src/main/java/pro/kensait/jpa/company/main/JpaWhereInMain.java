package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import pro.kensait.jpa.company.entity.Department;
import pro.kensait.jpa.company.entity.Employee;

/*
 * JPAwhere内の機能を提供するクラス
 */
@SuppressWarnings("unchecked")
public class JpaWhereInMain {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                {
                System.out.println("##### TEST1 START #####");
                List<Integer> paramList = new ArrayList<Integer>();
                paramList.add(1);
                paramList.add(4);
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee e " +
                        "WHERE e.department.departmentId IN :departmentId")
                        .setParameter("departmentId", paramList);
                List<Employee> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST1 END #####\n");
                }

                {
                System.out.println("##### TEST2 START #####");
                List<Department> paramList = new ArrayList<Department>();
                Department department1 = entityManager.find(Department.class, 1);
                Department department2 = entityManager.find(Department.class, 4);
                paramList.add(department1);
                paramList.add(department2);
                Query query = entityManager.createQuery(
                        "SELECT e FROM Employee e " +
                        "WHERE e.department IN :department")
                        .setParameter("department", paramList);
                List<Employee> resultList = query.getResultList();
                showEntityList(resultList); // 検索結果を表示
                System.out.println("##### TEST2#END #####\n");
                }
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
