package pro.kensait.course;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;

/*
 * JPAmappingのテスト
 */
class JpaMappingTest {
    // 「全マッピング済みエンティティによる初期データ参照」の検証
    @Test
    void readsInitialDataThroughEveryMappedEntity() throws Exception {
        SampleDatabase.reset();
        // 初期化を繰り返せることも確認する
        SampleDatabase.reset();
        try (var factory = Persistence.createEntityManagerFactory("MyPersistenceUnit");
                var entityManager = factory.createEntityManager()) {
            assertTrue(!factory.getMetamodel().getEntities().isEmpty());
            for (var entity : factory.getMetamodel().getEntities()) {
                // ネイティブSQLの結果専用Entityには対応する実テーブルがない
                if (entity.getName().startsWith("EmployeeQueryResult")) {
                    continue;
                }
                var rows = entityManager.createQuery("select e from " + entity.getName() + " e")
                        .getResultList();
                assertTrue(!rows.isEmpty(), entity.getName() + " must have seed data");
            }
        }
    }
}
