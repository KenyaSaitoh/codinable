package pro.kensait.db.mybatis;

import java.io.IOException;
import java.io.Reader;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

/*
 * mybatisを生成するファクトリー
 */
public final class MyBatisFactory {
    // mybatisの初期化
    private MyBatisFactory() {
    }

    // 新規データの登録
    public static SqlSessionFactory create() throws IOException {
        try (Reader reader = Resources.getResourceAsReader("mybatis-config.xml")) {
            return new SqlSessionFactoryBuilder().build(reader);
        }
    }
}
