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
import pro.kensait.jpa.company.entity.Department_;
import pro.kensait.jpa.company.entity.Employee;
import pro.kensait.jpa.company.entity.Employee_;

// クライテリアによる動的SQLのテスト（静的メタモデル版）
// CriteriaMain2 が employee.get("salary") のように属性名を文字列で書いていたところを、
// 注釈プロセッサ（hibernate-processor）がコンパイル時に生成する Employee_ / Department_ で書く。
// 属性名の綴りや型の誤りがコンパイルエラーとして分かる
public class CriteriaMain3 {

    public static void main(String[] args) {
        // SALES（部署ID3）に所属している社員（月給・氏名の条件なし）
        System.out.println("##### TEST1 START #####");
        executeQuery(3, null, null);
        System.out.println("##### TEST1 END #####\n");

        // SALES（部署ID3）に所属している月給が30万円以上の社員
        System.out.println("##### TEST2 START #####");
        executeQuery(3, 300000, null);
        System.out.println("##### TEST2 END #####\n");

        // 氏名に「a」を含む月給が30万円以上の社員（全部署）
        System.out.println("##### TEST3 START #####");
        executeQuery(null, 300000, "a");
        System.out.println("##### TEST3 END #####\n");

        // 氏名に「%」を含む社員（エスケープしているので、ワイルドカードではなく文字として扱われ 0 件）
        System.out.println("##### TEST4 START #####");
        executeQuery(null, null, "%");
        System.out.println("##### TEST4 END #####\n");
    }

    // クエリの実行（departmentId: 部署ID、lower: 月給の下限、namePart: 氏名の部分一致。null の条件は付けない）
    private static void executeQuery(Integer departmentId, Integer lower,
            String namePart) {

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
                // 属性は文字列ではなく、生成された静的メタモデルの属性で指定する
                Predicate condition = cb.conjunction();
                if (departmentId != null)
                    condition = cb.and(condition, cb.equal(
                            employee.get(Employee_.department).get(Department_.departmentId),
                            departmentId));
                if (lower != null)
                    condition = cb.and(condition, cb.ge(
                            employee.get(Employee_.salary), lower));
                if (namePart != null)
                    // 入力に含まれる % と _ を \ でエスケープし、第3引数でエスケープ文字を指定する
                    // （エスケープしないと、入力の % や _ がそのままワイルドカードになる）
                    condition = cb.and(condition, cb.like(
                            employee.get(Employee_.employeeName),
                            "%" + escapeLike(namePart) + "%", '\\'));

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

    // LIKE のワイルドカード（% と _）とエスケープ文字自身（\）を \ でエスケープする
    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
