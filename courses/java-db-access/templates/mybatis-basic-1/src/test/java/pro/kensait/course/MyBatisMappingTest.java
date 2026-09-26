package pro.kensait.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import pro.kensait.mybatis.company.common.SqlSessionHolder;

/*
 * mybatismappingのテスト
 */
class MyBatisMappingTest {
    // 「マッパー読み込みと初期データ参照」の検証
    @Test
    void loadsMappersAndReadsSeedData() throws Exception {
        SampleDatabase.reset();
        SampleDatabase.reset();
        try (var session = SqlSessionHolder.getInstance().getSqlSession()) {
            assertFalse(session.getConfiguration().getMappedStatementNames().isEmpty());
            // 全章共通の初期データは14人（未所属社員を含む）
            try (var statement = session.getConnection().createStatement();
                    var result = statement.executeQuery("SELECT COUNT(*) FROM EMPLOYEE")) {
                result.next();
                assertEquals(14, result.getInt(1));
            }
        }
    }
}
