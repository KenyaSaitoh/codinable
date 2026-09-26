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

//クライテリアによる動的SQLのテスト
public class CriteriaMain2 {

    public static void main(String[] args) {
        // SALES（部署ID3）に所属している社員（月給の条件なし）
        System.out.println("##### TEST1 START #####");
        executeQuery(3, null, null);
        System.out.println("##### TEST1 END #####\n");

        // SALES（部署ID3）に所属している月給が30万円以上の社員
        System.out.println("##### TEST2 START #####");
        executeQuery(3, 300000, null);
        System.out.println("##### TEST2 END #####\n");

        // SALES（部署ID3）に所属している月給が30万円以上、40万円以下の社員
        System.out.println("##### TEST3 START #####");
        executeQuery(3, 300000, 400000);
        System.out.println("##### TEST3 END #####\n");

        // 月給が30万円以上、40万円以下の社員（全部署）
        System.out.println("##### TEST4 START #####");
        executeQuery(null, 300000, 400000);
        System.out.println("##### TEST5 END #####\n");
    }

    // クエリの実行
    private static void executeQuery(Integer departmentId, Integer lower,
            Integer upper) {

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

                // パラメータにしたがって動的にPredicate（検索条件）を構築する
                Predicate condition = cb.conjunction();
                if (departmentId != null)
                    condition = cb.and(condition, cb.equal(
                            employee.get("department").get("departmentId"), departmentId));
                if (lower != null)
                    condition = cb.and(condition, cb.ge(
                            employee.get("salary"), lower));
                if (upper != null)
                    condition = cb.and(condition, cb.le(
                            employee.get("salary"), upper));

                // CriteriaQueryを作成する
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
