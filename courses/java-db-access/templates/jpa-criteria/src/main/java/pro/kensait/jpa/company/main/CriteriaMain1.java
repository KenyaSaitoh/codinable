package pro.kensait.jpa.company.main;

import static pro.kensait.jdbc.util.ResultUtil.*;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import pro.kensait.jpa.company.entity.Employee;

// クライテリアによる動的SQLのテスト
public class CriteriaMain1 {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // CriteriaBuilderを取得する
                CriteriaBuilder cb = entityManager.getCriteriaBuilder();

                // CriteriaQueryを取得する
                CriteriaQuery<Employee> cq = cb.createQuery(Employee.class);

                // Rootを取得する
                Root<Employee> employee = cq.from(Employee.class);

                // Predicate（検索条件）を構築する
                Predicate condition = cb.conjunction();
                condition = cb.and(condition, cb.equal(
                        employee.get("department").get("departmentId"), 1));
                condition = cb.and(condition, cb.ge(employee.get("salary"), 300000));

                // CriteriaQueryにRootと検索条件を設定する
                cq.select(employee).where(condition);

                // クエリを実行して結果を取得する
                List<Employee> resultList = entityManager.createQuery(cq)
                        .getResultList();

                // 検索結果を表示する
                showEntity(resultList);
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
